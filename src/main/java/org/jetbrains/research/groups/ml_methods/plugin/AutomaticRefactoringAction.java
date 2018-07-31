package org.jetbrains.research.groups.ml_methods.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.AnalysisUIOptions;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmResult;
import org.jetbrains.research.groups.ml_methods.config.ArchitectureReloadedConfig;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringExecutionContext;
import org.jetbrains.research.groups.ml_methods.ui.AlgorithmsSelectionPanel;
import org.jetbrains.research.groups.ml_methods.ui.RefactoringsToolWindow;
import org.jetbrains.research.groups.ml_methods.utils.ArchitectureReloadedBundle;
import org.jetbrains.research.groups.ml_methods.utils.MetricsProfilesUtil;
import org.jetbrains.research.groups.ml_methods.utils.NotificationUtil;
import org.jetbrains.research.groups.ml_methods.utils.RefactoringUtil;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This action is intended to be invoked when the user wants to know what refactorings he could
 * apply to improve his/her project.
 * As {@link BaseAnalysisAction}'s subclass its logical entry point is the {@link #analyze} method .
 * Nevertheless it has the {@link #actionPerformed} method to achieve desired behaviour in a hacky way.
 */
public class AutomaticRefactoringAction extends BaseAnalysisAction {
    private static final Logger LOGGER = Logging.getLogger(AutomaticRefactoringAction.class);
    private static final String REFACTORING_PROFILE_KEY = "refactoring.metrics.profile.name";
    private static final Map<String, ProgressIndicator> processes = new ConcurrentHashMap<>();

    private Map<String, Map<String, String>> results = new HashMap<>();

    private static final Map<Project, AutomaticRefactoringAction> factory = new HashMap<>();

//    private static ProjectManagerListener listener = new ProjectManagerListener() {
//        @Override
//        public void projectOpened(Project project) {
//            getInstance(project).analyzeBackground(project, new AnalysisScope(project),
//                    project.getName() + project.getLocationHash() + "|opened");
//        }
//
//        @Override
//        public void projectClosed(Project project) {
//            deleteInstance(project);
//        }
//    };
//
//    static {
//        // todo fix bug: IndexNotReadyException
////        ProjectManager.getInstance().addProjectManagerListener(listener);
//    }

    public AutomaticRefactoringAction() {
        super(ArchitectureReloadedBundle.message("refactorings.search"),
                ArchitectureReloadedBundle.message("analyzing"));
    }

    @NotNull
    public static AutomaticRefactoringAction getInstance(@NotNull Project project) {
        if (!factory.containsKey(project)) {
            PluginManager.getLogger().info("Creating refactoring action for project " + project.getName());
            factory.put(project, new AutomaticRefactoringAction());
        }
        return factory.get(project);
    }

//    private static void deleteInstance(@NotNull Project project) {
//        factory.remove(project);
//    }

    /**
     * Entry point of this action. Sets project's global flag
     * {@link AnalysisUIOptions#ANALYZE_TEST_SOURCES} to false in order to skip analysis of tests
     * in some cases. Restores this flag back after action has been executed.
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }

        AnalysisUIOptions UIOptions = AnalysisUIOptions.getInstance(project);
        boolean previousValue = UIOptions.ANALYZE_TEST_SOURCES;
        UIOptions.ANALYZE_TEST_SOURCES = false;

        super.actionPerformed(e);

        UIOptions.ANALYZE_TEST_SOURCES = previousValue;
    }

    /**
     * A logical entry point of this action.
     *
     * @param project current project this action is invoked for.
     * @param analysisScope scope (set of files) that must be analysed.
     */
    @Override
    protected void analyze(
        @NotNull final Project project,
        @NotNull final AnalysisScope analysisScope
    ) {
        if (analysisScope.getFileCount() == 0) {
            NotificationUtil.notifyEmptyScope(project);
            return;
        }

        LOGGER.info("Run analysis (scope=" + analysisScope.getDisplayName() + ")");

        Set<Algorithm> selectedAlgorithms =
                ArchitectureReloadedConfig.getInstance().getSelectedAlgorithms();

        final MetricsProfile metricsProfile = getMetricsProfile(selectedAlgorithms);
        assert metricsProfile != null;
        final boolean enableFieldRefactoring =
            ArchitectureReloadedConfig.getInstance().enableFieldRefactoring();

        new RefactoringExecutionContext(
            project,
            analysisScope,
            metricsProfile,
            selectedAlgorithms,
            enableFieldRefactoring,
            this::showDialogs
        ).executeAsync();
    }

    public void analyzeBackground(@NotNull final Project project, @NotNull final AnalysisScope analysisScope,
                                  String identifier) {
        List<Algorithm> availableAlgorithms = Arrays.asList(RefactoringExecutionContext.getAvailableAlgorithms());
        final MetricsProfile metricsProfile = getMetricsProfile(new HashSet<>(availableAlgorithms));
        assert metricsProfile != null;

        final RefactoringExecutionContext context =
                new RefactoringExecutionContext(project, analysisScope, metricsProfile, this::updateResults);
        processes.computeIfPresent(identifier, (x, process) -> {
            process.cancel();
            return null;
        });
        new Task.Backgroundable(project,
                "Calculating Refactorings...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                processes.put(identifier, indicator);
                context.executeSynchronously();
            }

            @Override
            public void onFinished() {
                super.onFinished();
                DaemonCodeAnalyzer.getInstance(project).restart();
            }

        }.queue();
    }

    private void updateResults(@NotNull RefactoringExecutionContext context) {
        for (AlgorithmResult result : context.getAlgorithmResults()) {
            results.put(result.getAlgorithmName(), RefactoringUtil.toMap(result.getRefactorings()));
        }
    }


    private void showDialogs(@NotNull RefactoringExecutionContext context) {
        updateResults(context);
        final Set<String> selectedAlgorithms =
            ArchitectureReloadedConfig.getInstance()
                .getSelectedAlgorithms()
                .stream()
                .map(Algorithm::getDescriptionString)
                .collect(Collectors.toSet());

        final List<AlgorithmResult> algorithmsResults = context.getAlgorithmResults()
                .stream()
                .filter(result -> selectedAlgorithms.contains(result.getAlgorithmName()))
                .collect(Collectors.toList());

        boolean notEmptyResult = algorithmsResults.stream().flatMap(result -> result.getRefactorings()
                .stream()).findAny().isPresent();
        
        if (notEmptyResult) {
            ServiceManager.getService(context.getProject(), RefactoringsToolWindow.class)
                    .show(algorithmsResults, context.getEntitySearchResult(), context.getScope());
        } else {
            NotificationUtil.notifyEmptyResult(context.getProject());
        }
    }

    private static void checkRefactoringProfile(final @NotNull Set<Algorithm> selectedAlgorithms) {
        final Set<Metric> requestedSet =
            selectedAlgorithms.stream()
                .flatMap(it -> it.requiredMetrics().stream())
                .collect(Collectors.toSet());

        final String profileName = ArchitectureReloadedBundle.message(REFACTORING_PROFILE_KEY);
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        if (MetricsProfilesUtil.checkMetricsList(profileName, requestedSet, repository)) {
            return;
        }
        MetricsProfilesUtil.removeProfileForName(profileName, repository);
        repository.addProfile(MetricsProfilesUtil.createProfile(profileName, requestedSet));
    }

    private static MetricsProfile getMetricsProfile(
        final @NotNull Set<Algorithm> selectedAlgorithms
    ) {
        checkRefactoringProfile(selectedAlgorithms);
        final String profileName = ArchitectureReloadedBundle.message(REFACTORING_PROFILE_KEY);
        return MetricsProfileRepository.getInstance().getProfileForName(profileName);
    }

    @NotNull
    public Set<String> calculatedAlgorithms() {
        return results.keySet();
    }

    @NotNull
    public Map<String, String> getRefactoringsForName(String algorithm) {
        if (!results.containsKey(algorithm)) {
            throw new IllegalArgumentException("Uncalculated algorithm requested: " + algorithm);
        }
        return results.get(algorithm);
    }

    @Override
    @Nullable
    protected JComponent getAdditionalActionSettings(Project project, BaseAnalysisActionDialog dialog) {
        return new AlgorithmsSelectionPanel();
    }
}

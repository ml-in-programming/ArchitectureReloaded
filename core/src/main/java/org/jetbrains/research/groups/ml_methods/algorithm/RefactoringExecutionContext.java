package org.jetbrains.research.groups.ml_methods.algorithm;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.AttributesStorage;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.NoRequestedMetricException;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.EntitiesStorage;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.EntitySearcher;
import org.jetbrains.research.groups.ml_methods.logging.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository.AlgorithmType;
import static org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository.getAvailableAlgorithms;

/**
 * Stores all information that might be needed for refactoring suggestions calculation. Can be
 * executed which means that it will calculate all information.
 */
public class RefactoringExecutionContext {
    private static final Logger LOGGER = Logging.getLogger(RefactoringExecutionContext.class);

    @NotNull
    private final MetricsRunImpl metricsRun = new MetricsRunImpl();
    private final Project project;
    private final AnalysisScope scope;
    @NotNull
    private final MetricsProfile profile;
    private EntitiesStorage entitiesStorage;
    private final MetricsExecutionContextImpl metricsExecutionContext;
    @Nullable
    private final Consumer<RefactoringExecutionContext> continuation;
    @NotNull
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<AlgorithmResult> algorithmsResults = new ArrayList<>();
    @NotNull
    private final Collection<Algorithm> requestedAlgorithms;
    private final boolean enableFieldRefactoring;

    public RefactoringExecutionContext(@NotNull Project project, @NotNull AnalysisScope scope,
                                       @NotNull MetricsProfile profile,
                                       @Nullable Consumer<RefactoringExecutionContext> continuation) {
        this(project, scope, profile, getAvailableAlgorithms(), true, continuation);
    }

    /**
     * Creates execution context by passing all needed data.
     *
     * @param project current project.
     * @param scope a scope which contains all files that should be processed by algorithms.
     * @param profile a profile of metrics that must be calculated.
     * @param requestedAlgorithms algorithm which were requested by user.
     * @param enableFieldRefactoring {@code True} if field refactoring is available.
     * @param continuation action that should be performed after all the calculations are done.
     */
    public RefactoringExecutionContext(@NotNull Project project, @NotNull AnalysisScope scope,
                                       @NotNull MetricsProfile profile,
                                       @NotNull Collection<Algorithm> requestedAlgorithms,
                                       boolean enableFieldRefactoring,
                                       @Nullable Consumer<RefactoringExecutionContext> continuation) {
        this.project = project;
        this.scope = scope;
        this.profile = profile;
        this.continuation = continuation;
        this.requestedAlgorithms = requestedAlgorithms;
        this.enableFieldRefactoring = enableFieldRefactoring;
        metricsExecutionContext = new MetricsExecutionContextImpl(project, scope);
    }

    /** Executes all calculations asynchronously.  */
    public void executeAsync() {
        Task.Modal task = new Task.Modal(project, "Search For Refactorings", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Search fo refactorings");
                execute(indicator);
            }

            @Override
            public void onSuccess() {
                RefactoringExecutionContext.this.onFinish();
            }
        };
        task.queue();
    }

    /** Executes all calculations synchronously. */
    public void executeSynchronously() {
        execute(new EmptyProgressIndicator());
        onFinish();
    }

    private void execute(ProgressIndicator indicator) {
        metricsExecutionContext.calculateMetrics(profile, metricsRun);
        metricsRun.setProfileName(profile.getName());
        metricsRun.setContext(scope);
        metricsRun.setTimestamp(new TimeStamp());
        entitiesStorage = ApplicationManager.getApplication()
                .runReadAction((Computable<EntitiesStorage>) () -> EntitySearcher.analyze(scope));
        for (Algorithm algorithm : requestedAlgorithms) {
            calculate(algorithm);
        }
        indicator.setText("Finish refactorings search...");
    }


    private void onFinish() {
        if (continuation != null) {
            continuation.accept(this);
        }
    }

    private void calculate(Algorithm algorithm) {
        AttributesStorage attributes;

        try {
            attributes = new AttributesStorage(
                    entitiesStorage,
                    algorithm.requiredMetrics(),
                    metricsRun
            );
        } catch (NoRequestedMetricException e) {
            LOGGER.error(
                "Error during attributes creation for '" + algorithm.getDescriptionString() +
                "' algorithm: " + e.getMessage() + " - " + algorithm.getDescriptionString() +
                "is aborted"
            );

            return;
        }

        final AlgorithmResult result =
            algorithm.execute(attributes, executorService, enableFieldRefactoring);

        if (result.isSuccess()) {
            algorithmsResults.add(result);
        }
    }

    public List<AlgorithmResult> getAlgorithmResults() {
        return new ArrayList<>(algorithmsResults);
    }

    public AlgorithmResult getResultForType(AlgorithmType algorithmType) {
        return algorithmsResults.stream()
                .filter(result -> algorithmType.equals(result.getAlgorithmType()))
                .findAny().orElse(null);
    }

    public EntitiesStorage getEntitiesStorage() {
        return entitiesStorage;
    }

    public @NotNull MetricsRun getMetricsRun() {
        return metricsRun;
    }

    public int getClassCount() {
        return entitiesStorage.getClasses().size();
    }

    public int getMethodsCount() {
        return entitiesStorage.getMethods().size();
    }

    public int getFieldsCount() {
        return entitiesStorage.getFields().size();
    }

    public Project getProject() {
        return project;
    }

    public AnalysisScope getScope() {
        return scope;
    }

    @NotNull
    public MetricsProfile getProfile() {
        return profile;
    }
}
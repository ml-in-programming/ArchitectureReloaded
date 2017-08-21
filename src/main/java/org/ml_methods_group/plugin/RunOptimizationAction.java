/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.AlgorithmResult;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.PropertiesStrategy;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.optimization.Optimizer;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;
import org.ml_methods_group.ui.AlgorithmsSelectionPanel;
import org.ml_methods_group.ui.RefactoringsToolWindow;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;
import org.ml_methods_group.utils.MetricsProfilesUtil;
import org.ml_methods_group.utils.RefactoringBase;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Collections.singletonList;

public class RunOptimizationAction extends BaseAnalysisAction {
    private final HashMap<String, String> allRefactorings = new HashMap<>();

    private static final Logger LOGGER = Logging.getLogger(AutomaticRefactoringAction.class);
    private static final String REFACTORING_PROFILE_KEY = "refactoring.metrics.profile.name";


    public RunOptimizationAction() {
        super(MetricsReloadedBundle.message("metrics.calculation"), MetricsReloadedBundle.message("metrics"));
    }

    @Override
    protected void analyze(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        LOGGER.info("Optimize analysis (scope=" + analysisScope.getDisplayName() + ")");

        final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance()
                .getCurrentProfile();
        assert metricsProfile != null;
        RefactoringBase base = RefactoringBase.getInstance(project);
        allRefactorings.clear();
        final String algorithm = "ARI";
        new Task.Backgroundable(project, "optimizing", false) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                new Optimizer(base, r -> runAlgorithm(r, project, analysisScope, base, algorithm))
                        .runOptimization();
            }
        }.queue();
    }

    private synchronized Map<String, String> runAlgorithm(PropertiesStrategy strategy, Project project, AnalysisScope scope,
                                            RefactoringBase base, String algorithm) {
        try {

            final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance()
                    .getCurrentProfile();
            assert metricsProfile != null;
            RefactoringExecutionContext context = new RefactoringExecutionContext(project, scope, metricsProfile,
                    algorithm, strategy);
            context.executeSynchronously();
            for (Entry<String, String> entity : context.getAlgorithmResults().get(0).getRefactorings().entrySet()) {
                if (base.getStatusFor(entity.getKey(), entity.getValue()) == RefactoringBase.Status.UNKNOWN) {
                    allRefactorings.put(entity.getKey(), entity.getValue());
                }
            }
            System.out.println("Algorithm finished");
            return context.getAlgorithmResults().get(0).getRefactorings();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private static void checkRefactoringProfile() {
        final Set<Class<? extends Metric>> requestedSet = Entity.getRequestedMetrics();
        final String profileName = ArchitectureReloadedBundle.message(REFACTORING_PROFILE_KEY);
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        if (MetricsProfilesUtil.checkMetricsList(profileName, requestedSet, repository)) {
            return;
        }
        MetricsProfilesUtil.removeProfileForName(profileName, repository);
        repository.addProfile(MetricsProfilesUtil.createProfile(profileName, requestedSet));
    }

    @Override
    @Nullable
    protected JComponent getAdditionalActionSettings(Project project, BaseAnalysisActionDialog dialog) {
        checkRefactoringProfile();
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new ProfileSelectionPanel(project), BorderLayout.SOUTH);
        panel.add(new AlgorithmsSelectionPanel(), BorderLayout.NORTH);
        return panel;
    }
}

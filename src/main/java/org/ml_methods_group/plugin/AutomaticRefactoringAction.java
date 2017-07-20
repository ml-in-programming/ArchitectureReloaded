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
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.profile.*;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.config.ArchitectureReloadedConfig;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;
import org.ml_methods_group.ui.AlgorithmsSelectionPanel;
import org.ml_methods_group.ui.RefactoringsToolWindow;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AutomaticRefactoringAction extends BaseAnalysisAction {
    private static final String REFACTORING_PROFILE_KEY = "refactoring.metrics.profile.name";

    private Map<String, Map<String, String>> refactorings = new HashMap<>();

    public AutomaticRefactoringAction() {
        super(MetricsReloadedBundle.message("metrics.calculation"), MetricsReloadedBundle.message("metrics"));
    }

    @Override
    protected void analyze(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        System.out.println(analysisScope.getDisplayName());
        System.out.println(project.getBasePath());
        System.out.println();

        final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance()
                .getCurrentProfile();
        assert metricsProfile != null;
        new RefactoringExecutionContext(project, analysisScope, metricsProfile,
                this::showRefactoringsDialog);
    }

    public void analyzeSynchronously(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        System.out.println(analysisScope.getDisplayName());
        System.out.println(project.getBasePath());
        System.out.println();

        checkRefactoringProfile();
        final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance()
                .getProfileForName(ArchitectureReloadedBundle.message(REFACTORING_PROFILE_KEY));
        assert metricsProfile != null;

        final RefactoringExecutionContext context =
                new RefactoringExecutionContext(project, analysisScope, metricsProfile);
        calculateRefactorings(context, true);
    }


    private void calculateRefactorings(@NotNull RefactoringExecutionContext context, boolean ignoreSelection) {
        final Set<String> selectedAlgorithms = ArchitectureReloadedConfig.getInstance().getSelectedAlgorithms();
        for (String algorithm : RefactoringExecutionContext.getAvailableAlgorithms()) {
            if (ignoreSelection || selectedAlgorithms.contains(algorithm)) {
                refactorings.put(algorithm, findRefactorings(algorithm, context));
            }
        }
    }

    private void showRefactoringsDialog(@NotNull RefactoringExecutionContext context) {
        calculateRefactorings(context, false);
        ServiceManager.getService(context.getProject(), MetricsToolWindow.class)
                .show(context.getMetricsRun(), context.getProfile(), context.getScope(), false);
        ServiceManager.getService(context.getProject(), RefactoringsToolWindow.class)
                .show(Collections.unmodifiableMap(refactorings), context.getScope());
    }

    private static void checkRefactoringProfile() {
        final Set<Class<? extends Metric>> requestedSet = Entity.getRequestedMetrics();
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        final String profileName = ArchitectureReloadedBundle.message(REFACTORING_PROFILE_KEY);
        final MetricsProfile refactoringProfile = repository.getProfileForName(profileName);
        if (refactoringProfile != null) {
            Set<Class<? extends Metric>> currentSet = refactoringProfile.getMetricInstances()
                    .stream()
                    .map(MetricInstance::getMetric)
                    .map(Metric::getClass)
                    .collect(Collectors.toSet());
            if (currentSet.equals(requestedSet)) {
                return;
            }
            repository.deleteProfile(refactoringProfile);
        }
        List<MetricInstance> metrics = new ArrayList<>();
        for (Class<? extends Metric> metricClass : requestedSet) {
            try {
                MetricInstance instance = new MetricInstanceImpl(metricClass.newInstance());
                instance.setEnabled(true);
                metrics.add(instance);
            } catch (Exception e) {
                System.out.println("Failed to create metric for name: " + metricClass.getCanonicalName());
            }
        }
        repository.addProfile(new MetricsProfileImpl(profileName, metrics));
    }

    private static Map<String, String> findRefactorings(String algorithm, RefactoringExecutionContext context) {
        try {
            return context.calculateAlgorithmForName(algorithm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    @NotNull
    public Set<String> calculatedAlgorithms() {
        return refactorings.keySet();
    }

    @NotNull
    public Map<String, String> getRefactoringsForName(String algorithm) {
        if (!refactorings.containsKey(algorithm)) {
            throw new IllegalArgumentException("Uncalculated algorithm requested: " + algorithm);
        }
        return refactorings.get(algorithm);
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

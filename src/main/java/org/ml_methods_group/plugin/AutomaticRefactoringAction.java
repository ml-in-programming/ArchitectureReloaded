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
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;
import org.ml_methods_group.ui.RefactoringDialog;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutomaticRefactoringAction extends BaseAnalysisAction {
    private static final Map<Project, AutomaticRefactoringAction> factory = new HashMap<>();
    private Map<String, String> refactoringsCCDA;
    private Map<String, String> refactoringsMRI;
    private Map<String, String> refactoringsAKMeans;
    private Map<String, String> refactoringsHAC;
    private Map<String, String> refactoringsARI;

    private static ProjectManagerListener listener = new ProjectManagerListener() {
        @Override
        public void projectOpened(Project project) {
            getInstance(project).analyzeBackground(project, new AnalysisScope(project));
        }

        @Override
        public void projectClosed(Project project) {
            deleteInstance(project);
        }
    };

    static {
        ProjectManager.getInstance().addProjectManagerListener(listener);
    }

    public AutomaticRefactoringAction() {
        super(MetricsReloadedBundle.message("metrics.calculation"), MetricsReloadedBundle.message("metrics"));
    }

    @NotNull
    public static AutomaticRefactoringAction getInstance(@NotNull Project project) {
        if (!factory.containsKey(project)) {
            PluginManager.getLogger().info("Creating refactoring action for project " + project.getName());
            factory.put(project, new AutomaticRefactoringAction());
        }
        return factory.get(project);
    }

    private static void deleteInstance(@NotNull Project project) {
        factory.remove(project);
    }

    @Override
    protected void analyze(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        analyze(project, analysisScope, this::showRefactoringsDialog);
    }

    private void analyze(@NotNull final Project project,
                         @NotNull final AnalysisScope analysisScope,
                         @Nullable Consumer<RefactoringExecutionContext> callback) {
        System.out.println(analysisScope.getDisplayName());
        System.out.println(project.getBasePath());
        System.out.println();

        final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance()
                .getCurrentProfile();
        assert metricsProfile != null;

        new RefactoringExecutionContext(project, analysisScope, metricsProfile,
                callback);
    }

    public void analyzeBackground(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        final Task.Backgroundable task = new Task.Backgroundable(project,
        "Calculating Refactorings...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                analyze(project, analysisScope, context -> {
                    new Task.Backgroundable(project, MetricsReloadedBundle.message("calculating.refactorings"), true) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            calculateRefactorings(context);
                        }

                        @Override
                        public void onFinished() {
                            super.onFinished();
                            DaemonCodeAnalyzer.getInstance(project).restart();
                        }
                    }.queue();
                });
            }
        };
        task.queue();
    }

    private void calculateRefactorings(@NotNull RefactoringExecutionContext context) {
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        AccessToken token = null;
        try {
            token = ApplicationManager.getApplication().acquireReadActionLock();
            refactoringsCCDA = findRefactorings(context::calculateCCDA);
            refactoringsMRI = findRefactorings(context::calculateMRI);
            refactoringsAKMeans = findRefactorings(context::calculateAKMeans);
            refactoringsHAC = findRefactorings(context::calculateHAC);
            refactoringsARI = findRefactorings(context::calculateARI);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (token != null) {
                token.finish();
            }
        }
    }

    private void showRefactoringsDialog(@NotNull RefactoringExecutionContext context) {
        calculateRefactorings(context);
        new RefactoringDialog(context.getProject(), context.getScope())
                .addSolution("CCDA", refactoringsCCDA)
                .addSolution("MRI", refactoringsMRI)
                .addSolution("AKMeans", refactoringsAKMeans)
                .addSolution("HAC", refactoringsHAC)
                .addSolution("ARI", refactoringsARI)
                .show();
    }

    private static Map<String, String> findRefactorings(@NotNull Supplier<Map<String, String>> algorithm) {
        try {
            return algorithm.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    public Map<String, String> getRefactoringsARI() {
        return refactoringsARI;
    }

    public Map<String, String> getRefactoringsCCDA() {
        return refactoringsCCDA;
    }

    public Map<String, String> getRefactoringsMRI() {
        return refactoringsMRI;
    }

    public Map<String, String> getRefactoringsAKMeans() {
        return refactoringsAKMeans;
    }

    public Map<String, String> getRefactoringsHAC() {
        return refactoringsHAC;
    }

    @Override
    @Nullable
    protected JComponent getAdditionalActionSettings(Project project, BaseAnalysisActionDialog dialog) {
        return new ProfileSelectionPanel(project);
    }
}

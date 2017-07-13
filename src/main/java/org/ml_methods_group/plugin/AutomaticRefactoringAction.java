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
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;

import javax.swing.*;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class AutomaticRefactoringAction extends BaseAnalysisAction {
    private Map<String, String> refactoringsCCDA;
    private Map<String, String> refactoringsMRI;
    private Map<String, String> refactoringsAKMeans;
    private Map<String, String> refactoringsHAC;
    private Map<String, String> refactoringsARI;

    public AutomaticRefactoringAction() {
        super(MetricsReloadedBundle.message("metrics.calculation"), MetricsReloadedBundle.message("metrics"));
    }

    @Override
    protected void analyze(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        System.out.println(analysisScope.getDisplayName());
        System.out.println(project.getBasePath());
        System.out.println();

        final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance().getCurrentProfile();
        assert metricsProfile != null;

        new RefactoringExecutionContext(project, analysisScope, metricsProfile, true
                , this::calculateRefactorings);
    }

    public void analyzeSynchronously(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        System.out.println(analysisScope.getDisplayName());
        System.out.println(project.getBasePath());
        System.out.println();

        final MetricsProfile metricsProfile = MetricsProfileRepository.getInstance().getCurrentProfile();
        assert metricsProfile != null;

        final RefactoringExecutionContext context =
                new RefactoringExecutionContext(project, analysisScope, metricsProfile);
        calculateRefactorings(context);
    }


    private void calculateRefactorings(@NotNull RefactoringExecutionContext context) {
        refactoringsCCDA = findRefactorings(context::calculateCCDA);
        refactoringsMRI = findRefactorings(context::calculateMRI);
        refactoringsAKMeans = findRefactorings(context::calculateAKMeans);
        refactoringsHAC = findRefactorings(context::calculateHAC);
        refactoringsARI = findRefactorings(context::calculateARI);
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

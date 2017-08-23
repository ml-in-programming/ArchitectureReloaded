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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.AlgorithmResult;
import org.ml_methods_group.algorithm.LabelPropagationAdapter;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;
import org.ml_methods_group.ui.RefactoringsToolWindow;
import org.ml_methods_group.utils.LabeledRefactorings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

public class LabelPropagationAction extends BaseAnalysisAction {

    private Project project;
    private final BiPredicate<MethodEntity, ClassEntity> isRefactoringAllowable =
            (m, c) -> m.getRelevantProperties().sizeOfIntersection(c.getRelevantProperties()) > 1;

    protected LabelPropagationAction() {
        super("Run LP", "LP");
    }

    @Override
    protected void analyze(@NotNull Project project, @NotNull AnalysisScope scope) {
        this.project = project;
        MetricsProfile profile = MetricsProfileRepository.getInstance().getCurrentProfile();
        new RefactoringExecutionContext(project, scope, profile, Collections.emptyList(), this::runLP).executeAsync();
    }

    private void runLP(RefactoringExecutionContext context) {
        final List<AlgorithmResult> results = new ArrayList<>();
        final LabelPropagationAdapter lp = new LabelPropagationAdapter();
        lp.calculate(context.getEntitySearchResult(),
                LabeledRefactorings.getLabeledRefactorings(),
                isRefactoringAllowable);
        results.add(new AlgorithmResult(lp.getResult(), "LP", 0, 0));
        ServiceManager.getService(context.getProject(), RefactoringsToolWindow.class)
                .show(results, context.getEntitySearchResult(), context.getScope(), null);
    }
}

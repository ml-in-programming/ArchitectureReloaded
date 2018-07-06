/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package com.sixrr.stockmetrics.classCalculators;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.sixrr.stockmetrics.projectCalculators.ElementCountProjectCalculator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeasureOfAggregationClassCalculator extends ClassCalculator {
    private Set<PsiClass> userDefinedClasses = new HashSet<>();

    @Override
    protected PsiElementVisitor createVisitor() {
        collectUserDefinedClasses();
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitClass(PsiClass aClass) {
            postMetric(aClass, (int)Stream.of(aClass.getFields()).map(PsiVariable::getType).
                    map(PsiTypesUtil::getPsiClass).filter(Objects::nonNull).
                    filter(x -> userDefinedClasses.contains(x)).count());
        }
    }

    private void collectUserDefinedClasses() {
        final Runnable runnable = () -> {
            Project project = executionContext.getProject();
            userDefinedClasses = FileBasedIndex.getInstance()
                    .getContainingFiles(
                            FileTypeIndex.NAME,
                            JavaFileType.INSTANCE,
                            ProjectScope.getProjectScope(project)).stream().
                            map(x -> PsiManager.getInstance(project).findFile(x)).
            filter(x -> x instanceof PsiJavaFile).map(x -> (PsiJavaFile)x).
                    flatMap(x -> Stream.of(x.getClasses())).
                    collect(Collectors.toSet());


        };
        final ProgressManager progressManager = ProgressManager.getInstance();
        progressManager.runProcess(runnable, null);
    }


}
package org.jetbrains.research.groups.ml_methods.vectorization;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.properties.finder_strategy.FinderStrategy;
import org.jetbrains.research.groups.ml_methods.algorithm.properties.finder_strategy.NewStrategy;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JBRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveMethodRefactoring;

import java.util.ArrayList;
import java.util.List;

public class DlbVectorization extends AbstractVectorization {
    @NotNull
    @Override
    protected List<? extends Vector> vectorize(@NotNull AnalysisScope scope) {
        ClassesAndMethodsVisitor visitor = new ClassesAndMethodsVisitor();
        scope.accept(visitor);
        List<DlbRefactoringVector> vectors = new ArrayList<>();
        for (PsiClass aClass : visitor.classes) {
            for (PsiMethod method : visitor.methods) {
                // TODO: check that refactoring can be applied
                JBRefactoringTextRepresentation refactoring =
                        new JBRefactoringTextRepresentation(new MoveMethodRefactoring(method, aClass));
                vectors.add(new DlbRefactoringVector(refactoring, 1, 0));
            }
        }
        return vectors;
    }

    private class ClassesAndMethodsVisitor extends JavaRecursiveElementVisitor {
        private final @NotNull
        List<PsiClass> classes = new ArrayList<>();
        private final @NotNull
        List<PsiMethod> methods = new ArrayList<>();
        private final @NotNull
        FinderStrategy strategy = NewStrategy.getInstance();

        @Override
        public void visitClass(PsiClass aClass) {
            if (strategy.acceptClass(aClass)) {
                classes.add(aClass);
            }
            super.visitClass(aClass);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            if (strategy.acceptMethod(method)) {
                methods.add(method);
            }
            super.visitMethod(method);
        }
    }
}

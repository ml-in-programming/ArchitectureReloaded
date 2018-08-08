package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.research.groups.ml_methods.ScopeAbstractTest;

import java.util.Map;

public abstract class AbstractGenerationConstraintTest extends ScopeAbstractTest {
    private final GenerationConstraint constraint = getTestingConstraint();
    private final Map<RefactoringTestRepresentation, Boolean> expectedRefactoringResults = getExpectedRefactoringResults();
    private final Map<MethodTestRepresentation, Boolean> expectedMethodResults = getExpectedMethodResults();
    private final Map<ClassTestRepresentation, Boolean> expectedClassResults = getExpectedClassResults();

    protected static class ClassTestRepresentation {
        private String className;

        public ClassTestRepresentation(String className) {
            this.className = className;
        }
    }

    protected static class MethodTestRepresentation {
        private String className;
        private String methodName;

        public MethodTestRepresentation(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }
    }

    protected static class RefactoringTestRepresentation {
        private String sourceClassName;
        private String methodName;
        private String targetClassName;

        public RefactoringTestRepresentation(String sourceClassName, String methodName, String targetClassName) {
            this.sourceClassName = sourceClassName;
            this.methodName = methodName;
            this.targetClassName = targetClassName;
        }
    }

    public void executeTest() {
        expectedClassResults.forEach(this::checkClass);
        expectedMethodResults.forEach(this::checkMethod);
        expectedRefactoringResults.forEach(this::checkRefactoring);
    }

    private void checkMethod(MethodTestRepresentation method, boolean expected) {
        PsiMethod psiMethod = myFixture.findClass(method.className).findMethodsByName(method.methodName, false)[0];
        assertEquals(expected, constraint.acceptMethod(psiMethod, getScope()));
    }

    private void checkClass(ClassTestRepresentation aClass, boolean expected) {
        PsiClass psiClass = myFixture.findClass(aClass.className);
        assertEquals(expected, constraint.acceptTargetClass(psiClass));
    }

    private void checkRefactoring(RefactoringTestRepresentation refactoring, boolean expected) {
        PsiMethod psiMethod = myFixture.findClass(refactoring.sourceClassName).
                findMethodsByName(refactoring.methodName, false)[0];
        PsiClass psiClass = myFixture.findClass(refactoring.targetClassName);
        assertEquals(expected, constraint.acceptRefactoring(psiMethod, psiClass));
    }

    abstract AnalysisScope getScope();
    abstract Map<MethodTestRepresentation, Boolean> getExpectedMethodResults();
    abstract Map<ClassTestRepresentation, Boolean> getExpectedClassResults();
    abstract Map<RefactoringTestRepresentation, Boolean> getExpectedRefactoringResults();
    abstract GenerationConstraint getTestingConstraint();
}
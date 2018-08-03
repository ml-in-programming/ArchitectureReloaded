package org.jetbrains.research.groups.ml_methods.generation;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraint;

import java.util.*;

public class RefactoringsGenerator {
    public static List<MoveMethodRefactoring> generate(Map<GenerationConstraint, Integer> toGenerate,
                                            AnalysisScope scope) {
        return new RefactoringsGeneratorVisitor(toGenerate, scope).generate();
    }

    public static List<MoveMethodRefactoring> generate(GenerationConstraint constraint, Integer numberOfRefactorings,
                                            AnalysisScope scope) {
        return generate(new HashMap<GenerationConstraint, Integer>(){{put(constraint, numberOfRefactorings);}}, scope);
    }

    public static MoveMethodRefactoring generate(GenerationConstraint constraint, AnalysisScope scope) {
        return generate(constraint, 1, scope).iterator().next();
    }

    private static class RefactoringsGeneratorVisitor extends JavaRecursiveElementVisitor {
        private static final @NotNull
        Logger LOGGER =
                Logger.getLogger(RefactoringsGenerationApplicationStarter.class);

        static {
            LOGGER.setLevel(Level.INFO);
            LOGGER.addAppender(new ConsoleAppender(new PatternLayout("%p %m%n")));
        }

        private Map<GenerationConstraint, Integer> toGenerate;
        private final AnalysisScope scope;
        private final Map<GenerationConstraint, List<PsiMethod>> acceptedMethods = new HashMap<>();
        private final Map<GenerationConstraint, List<PsiClass>> acceptedTargetClasses = new HashMap<>();
        private final Map<GenerationConstraint, List<MoveMethodRefactoring>> acceptedRefactorings = new HashMap<>();

        RefactoringsGeneratorVisitor(Map<GenerationConstraint, Integer> toGenerate, AnalysisScope scope) {
            this.toGenerate = toGenerate;
            this.scope = scope;
            initializeAllConstraints();
        }

        @Override
        public void visitClass(PsiClass aClass) {
            LOGGER.info("visited class: " + aClass.getQualifiedName());
            toGenerate.forEach((constraint, integer) -> {
                if (constraint.acceptTargetClass(aClass)) {
                    acceptedTargetClasses.get(constraint).add(aClass);
                }
            });
            super.visitClass(aClass);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            LOGGER.info("visited method: " + method.getName());
            toGenerate.forEach((constraint, integer) -> {
                if (constraint.acceptMethod(method, scope)) {
                    acceptedMethods.get(constraint).add(method);
                }
            });
            super.visitMethod(method);
        }

        List<MoveMethodRefactoring> generate() {
            LOGGER.info("Started walking through PSI Tree");
            scope.accept(this);
            LOGGER.info("Started creating refactorings");
            createRefactoringsAfterVisitingPsiTree();
            LOGGER.info("Started picking up random refactorings");
            return getRandomRefactorings();
        }

        private void initializeAllConstraints() {
            for (GenerationConstraint constraint : toGenerate.keySet()) {
                acceptedTargetClasses.put(constraint, new LinkedList<>());
                acceptedMethods.put(constraint, new LinkedList<>());
                acceptedRefactorings.put(constraint, new LinkedList<>());
            }
        }

        private List<MoveMethodRefactoring> getRandomRefactorings() {
            final List<MoveMethodRefactoring> generatedRefactorings = new LinkedList<>();

            toGenerate.forEach((constraint, number) -> {
                List<MoveMethodRefactoring> refactorings = acceptedRefactorings.get(constraint);
                Collections.shuffle(refactorings);
                if (refactorings.size() < number) {
                    throw new IllegalStateException("Cannot find for constraint enough refactorings, only " +
                            refactorings.size() + " found.");
                }
                generatedRefactorings.addAll(refactorings.subList(0, number));
            });
            return generatedRefactorings;
        }

        private void createRefactoringsAfterVisitingPsiTree() {
            for (GenerationConstraint constraint : toGenerate.keySet()) {
                LOGGER.info("New constraint");
                List<PsiClass> acceptedClassesForConstraint = acceptedTargetClasses.get(constraint);
                List<PsiMethod> acceptedMethodsForConstraint = acceptedMethods.get(constraint);
                List<MoveMethodRefactoring> acceptedRefactoringsForConstraint = acceptedRefactorings.get(constraint);
                LOGGER.info("Classes: "+ acceptedClassesForConstraint.size());
                LOGGER.info("Methods: "+ acceptedMethodsForConstraint.size());
                for (PsiClass acceptedClass: acceptedClassesForConstraint) {
                    LOGGER.info("New class: " + acceptedClass.getQualifiedName());
                    for (PsiMethod acceptedMethod : acceptedMethodsForConstraint) {
                        if (constraint.acceptRefactoring(acceptedMethod, acceptedClass)) {
                            acceptedRefactoringsForConstraint.add(new MoveMethodRefactoring(acceptedMethod, acceptedClass));
                        }
                    }
                }
            }
        }
    }
}

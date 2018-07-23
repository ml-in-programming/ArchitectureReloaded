package org.jetbrains.research.groups.ml_methods.generation;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraint;

import java.util.*;

public class RefactoringsGenerator {
    public static Set<Refactoring> generate(Map<GenerationConstraint, Integer> toGenerate,
                                            AnalysisScope scope) {
        return new RefactoringsGeneratorVisitor(toGenerate, scope).generate();
    }

    public static Set<Refactoring> generate(GenerationConstraint constraint, Integer numberOfRefactorings,
                                            AnalysisScope scope) {
        return generate(new HashMap<GenerationConstraint, Integer>(){{put(constraint, numberOfRefactorings);}}, scope);
    }

    public static Refactoring generate(GenerationConstraint constraint, AnalysisScope scope) {
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
        private final Map<GenerationConstraint, Set<PsiMethod>> acceptedMethods = new HashMap<>();
        private final Map<GenerationConstraint, Set<PsiField>> acceptedFields = new HashMap<>();
        private final Map<GenerationConstraint, Set<PsiClass>> acceptedTargetClasses = new HashMap<>();
        private final Map<GenerationConstraint, Set<Refactoring>> acceptedRefactorings = new HashMap<>();

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
                if (constraint.acceptMethod(method)) {
                    acceptedMethods.get(constraint).add(method);
                }
            });
            super.visitMethod(method);
        }

        @Override
        public void visitField(PsiField field) {
            LOGGER.info("visited field: " + field.getName());
            toGenerate.forEach((constraint, integer) -> {
                if (constraint.acceptField(field)) {
                    acceptedFields.get(constraint).add(field);
                }
            });
            super.visitField(field);
        }

        Set<Refactoring> generate() {
            LOGGER.info("Started walking through PSI Tree");
            scope.accept(this);
            LOGGER.info("Started creating refactorings");
            createRefactoringsAfterVisitingPsiTree();
            LOGGER.info("Started picking up random refactorings");
            return getRandomRefactorings();
        }

        private void initializeAllConstraints() {
            for (GenerationConstraint constraint : toGenerate.keySet()) {
                acceptedTargetClasses.put(constraint, new HashSet<>());
                acceptedMethods.put(constraint, new HashSet<>());
                acceptedFields.put(constraint, new HashSet<>());
                acceptedRefactorings.put(constraint, new HashSet<>());
            }
        }

        private Set<Refactoring> getRandomRefactorings() {
            final Set<Refactoring> generatedRefactorings = new HashSet<>();

            toGenerate.forEach((constraint, number) -> {
                List<Refactoring> refactorings = new ArrayList<>(acceptedRefactorings.get(constraint));
                Collections.shuffle(refactorings);
                generatedRefactorings.addAll(refactorings.subList(0, number));
            });
            return generatedRefactorings;
        }

        private void createRefactoringsAfterVisitingPsiTree() {
            for (GenerationConstraint constraint : toGenerate.keySet()) {
                LOGGER.info("New constraint");
                Set<PsiClass> acceptedClassesForConstraint = acceptedTargetClasses.get(constraint);
                Set<PsiMethod> acceptedMethodsForConstraint = acceptedMethods.get(constraint);
                Set<Refactoring> acceptedRefactoringsForConstraint = acceptedRefactorings.get(constraint);
                LOGGER.info("Classes: "+ acceptedClassesForConstraint.size());
                LOGGER.info("Methods: "+ acceptedMethodsForConstraint.size());
                for (PsiClass acceptedClass: acceptedClassesForConstraint) {
                    LOGGER.info("New class");
                    for (PsiMethod acceptedMethod : acceptedMethodsForConstraint) {
                        if (constraint.acceptRefactoring(acceptedMethod, acceptedClass)) {
                            acceptedRefactoringsForConstraint.add(new Refactoring(acceptedMethod, acceptedClass));
                        }
                    }
                }
            }
        }
    }
}

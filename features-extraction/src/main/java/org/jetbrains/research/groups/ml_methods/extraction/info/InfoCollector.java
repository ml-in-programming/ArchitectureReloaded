package org.jetbrains.research.groups.ml_methods.extraction.info;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;

public class InfoCollector {
    private static final @NotNull InfoCollector INSTANCE = new InfoCollector();

    private static final @NotNull Logger LOGGER = Logger.getLogger(InfoCollector.class);

    static {
        LOGGER.setLevel(Level.DEBUG);
        LOGGER.addAppender(new ConsoleAppender(new PatternLayout("%p [%c.%M] - %m%n")));
    }

    private InfoCollector() {}

    public static @NotNull InfoCollector getInstance() {
        return INSTANCE;
    }

    /**
     * Collects information from given {@link AnalysisScope} and creates
     * {@link MethodInfoRepository} as a result.
     *
     * @param scope a scope which will be analyzed.
     * @return all gathered info in a {@link MethodInfoRepository} object.
     */
    public @NotNull MethodInfoRepository collectInfo(final @NotNull AnalysisScope scope) {
        MethodInfoRepository.Builder repositoryBuilder = new MethodInfoRepository.Builder();

        scope.accept(new JavaRecursiveElementVisitor() {
            private PsiMethod currentMethod = null;

            public void visitMethod(PsiMethod method) {
                currentMethod = method;
                repositoryBuilder.addMethod(method);

                super.visitMethod(method);

                currentMethod = null;
            }

            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                if (currentMethod == null) {
                    super.visitMethodCallExpression(expression);
                    return;
                }

                PsiMethod target = expression.resolveMethod();

                if (target == null) {
                    LOGGER.warn(
                        "Failed to resolve method call: " +
                        expression.getText() + "; in " +
                        expression.getContainingFile()
                    );
                    
                    return;
                }

                repositoryBuilder.addMethodCall(currentMethod, target, expression);
                super.visitMethodCallExpression(expression);
            }

            public void visitReferenceExpression(PsiReferenceExpression expression) {
                if (currentMethod == null) {
                    super.visitReferenceExpression(expression);
                    return;
                }

                JavaResolveResult result = expression.advancedResolve(false);
                PsiElement element = result.getElement();

                if (element instanceof PsiField) {
                    repositoryBuilder.addFieldAccess(currentMethod, (PsiField) element);
                }

                super.visitReferenceExpression(expression);
            }
        });

        return repositoryBuilder.build();
    }
}

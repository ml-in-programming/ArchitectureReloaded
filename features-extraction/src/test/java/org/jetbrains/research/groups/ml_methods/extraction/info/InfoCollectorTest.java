package org.jetbrains.research.groups.ml_methods.extraction.info;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.ScopeAbstractTest;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InfoCollectorTest extends ScopeAbstractTest {
    public void testSingleMethod() throws Exception {
        runTestCase(new AbstractInfoValidator() {
            @Override
            public @NotNull List<String> methodNames() {
                return Collections.singletonList("singleMethod.A.method()");
            }
        });
    }

    public void testSimpleFieldAccess() throws Exception {
        runTestCase(new AbstractInfoValidator() {
            @Override
            public @NotNull List<String> methodNames() {
                return Collections.singletonList("simpleFieldAccess.A.method()");
            }

            @Override
            public @NotNull List<String> getAccessedFields(@NotNull String methodName) {
                return Collections.singletonList("simpleFieldAccess.A.field");
            }
        });
    }

    public void testForeignFieldAccess() throws Exception {
        runTestCase(new AbstractInfoValidator() {
            @Override
            public @NotNull List<String> methodNames() {
                return Collections.singletonList("foreignFieldAccess.A.method()");
            }

            @Override
            public @NotNull List<String> getAccessedFields(@NotNull String methodName) {
                return Collections.singletonList("foreignFieldAccess.B.field");
            }
        });
    }

    public void testSameObjectCall() throws Exception {
        runTestCase(new AbstractInfoValidator() {
            @Override
            public @NotNull List<String> methodNames() {
                return Arrays.asList("sameObjectCall.A.method1()", "sameObjectCall.A.method2()");
            }

            public @NotNull List<String> getSameObjectCallers(@NotNull String methodName) {
                if (methodName.equals("sameObjectCall.A.method2()")) {
                    return Collections.singletonList("sameObjectCall.A.method1()");
                }

                return Collections.emptyList();
            }

            public @NotNull List<String> getSameObjectTargets(@NotNull String methodName) {
                if (methodName.equals("sameObjectCall.A.method1()")) {
                    return Collections.singletonList("sameObjectCall.A.method2()");
                }

                return Collections.emptyList();
            }
        });
    }

    public void testAnotherObjectCall() throws Exception {
        runTestCase(new AbstractInfoValidator() {
            @Override
            public @NotNull List<String> methodNames() {
                return Arrays.asList("anotherObjectCall.A.method1()", "anotherObjectCall.A.method2()");
            }

            public @NotNull List<String> getAnotherObjectCallers(@NotNull String methodName) {
                if (methodName.equals("anotherObjectCall.A.method2()")) {
                    return Collections.singletonList("anotherObjectCall.A.method1()");
                }

                return Collections.emptyList();
            }

            public @NotNull List<String> getAnotherObjectTargets(@NotNull String methodName) {
                if (methodName.equals("anotherObjectCall.A.method1()")) {
                    return Collections.singletonList("anotherObjectCall.A.method2()");
                }

                return Collections.emptyList();
            }
        });
    }

    private void runTestCase(final @NotNull InfoValidator validator) throws Exception {
        AnalysisScope scope = createScope();
        MethodInfoRepository repository = InfoCollector.getInstance().collectInfo(scope);

        assertSameElements(
            repository.getMethods()
                      .stream()
                      .map(MethodUtils::calculateUniqueSignature)
                      .collect(Collectors.toList()),
            validator.methodNames()
        );

        for (PsiMethod method : repository.getMethods()) {
            String methodName = MethodUtils.calculateUniqueSignature(method);

            MethodInfo info = repository.getMethodInfo(method).orElseThrow(
                () -> new IllegalStateException(
                    "Existing method doesn't have MethodInfo instance"
                )
            );

            assertSameElements(
                info.getSameInstanceCallers()
                    .stream()
                    .map(MethodUtils::calculateUniqueSignature)
                    .collect(Collectors.toList()),
                validator.getSameObjectCallers(methodName)
            );

            assertSameElements(
                info.getAnotherInstanceCallers()
                    .stream()
                    .map(MethodUtils::calculateUniqueSignature)
                    .collect(Collectors.toList()),
                validator.getAnotherObjectCallers(methodName)
            );

            assertSameElements(
                info.getSameInstanceTargets()
                    .stream()
                    .map(MethodUtils::calculateUniqueSignature)
                    .collect(Collectors.toList()),
                validator.getSameObjectTargets(methodName)
            );

            assertSameElements(
                info.getAnotherInstanceTargets()
                    .stream()
                    .map(MethodUtils::calculateUniqueSignature)
                    .collect(Collectors.toList()),
                validator.getAnotherObjectTargets(methodName)
            );

            assertSameElements(
                info.getAccessedFields()
                    .stream()
                    .map(PSIUtil::getUniqueName)
                    .collect(Collectors.toList()),
                validator.getAccessedFields(methodName)
            );
        }
    }

    private interface InfoValidator {
        @NotNull List<String> methodNames();

        @NotNull List<String> getSameObjectCallers(@NotNull String methodName);

        @NotNull List<String> getAnotherObjectCallers(@NotNull String methodName);

        @NotNull List<String> getSameObjectTargets(@NotNull String methodName);

        @NotNull List<String> getAnotherObjectTargets(@NotNull String methodName);

        @NotNull List<String> getAccessedFields(@NotNull String methodName);
    }

    private static abstract class AbstractInfoValidator implements InfoValidator {
        public @NotNull List<String> getSameObjectCallers(@NotNull String methodName) {
            return Collections.emptyList();
        }

        public @NotNull List<String> getAnotherObjectCallers(@NotNull String methodName) {
            return Collections.emptyList();
        }

        public @NotNull List<String> getSameObjectTargets(@NotNull String methodName) {
            return Collections.emptyList();
        }

        public @NotNull List<String> getAnotherObjectTargets(@NotNull String methodName) {
            return Collections.emptyList();
        }

        public @NotNull List<String> getAccessedFields(@NotNull String methodName) {
            return Collections.emptyList();
        }
    }
}

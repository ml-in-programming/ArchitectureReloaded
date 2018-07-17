package org.jetbrains.research.groups.ml_methods;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InfoCollectorTest extends ScopeAbstractTest {
    public void testSingleMethod() throws Exception {
        runTestCase(new InfoValidator() {
            @Override
            public @NotNull List<String> methodNames() {
                return Collections.singletonList("singleMethod.A.method()");
            }

            @NotNull
            @Override
            public List<String> getSameObjectCallers(@NotNull String methodName) {
                return Collections.emptyList();
            }

            @NotNull
            @Override
            public List<String> getAnotherObjectCallers(@NotNull String methodName) {
                return Collections.emptyList();
            }

            @NotNull
            @Override
            public List<String> getSameObjectTargets(@NotNull String methodName) {
                return Collections.emptyList();
            }

            @NotNull
            @Override
            public List<String> getAnotherObjectTargets(@NotNull String methodName) {
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
                      .map(MethodUtils::calculateSignature)
                      .collect(Collectors.toList()),
            validator.methodNames()
        );

        for (PsiMethod method : repository.getMethods()) {
            String methodName = MethodUtils.calculateSignature(method);

            MethodInfo info = repository.getMethodInfo(method).orElseThrow(
                () -> new IllegalStateException(
                    "Existing method doesn't have MethodInfo instance"
                )
            );

            assertSameElements(
                info.getSameObjectCallers()
                    .stream()
                    .map(MethodUtils::calculateSignature)
                    .collect(Collectors.toList()),
                validator.getSameObjectCallers(methodName)
            );

            assertSameElements(
                info.getAnotherObjectCallers()
                    .stream()
                    .map(MethodUtils::calculateSignature)
                    .collect(Collectors.toList()),
                validator.getAnotherObjectCallers(methodName)
            );

            assertSameElements(
                info.getSameObjectTargets()
                    .stream()
                    .map(MethodUtils::calculateSignature)
                    .collect(Collectors.toList()),
                validator.getSameObjectTargets(methodName)
            );

            assertSameElements(
                info.getAnotherObjectTargets()
                    .stream()
                    .map(MethodUtils::calculateSignature)
                    .collect(Collectors.toList()),
                validator.getAnotherObjectTargets(methodName)
            );
        }
    }

    private interface InfoValidator {
        @NotNull List<String> methodNames();

        @NotNull List<String> getSameObjectCallers(@NotNull String methodName);

        @NotNull List<String> getAnotherObjectCallers(@NotNull String methodName);

        @NotNull List<String> getSameObjectTargets(@NotNull String methodName);

        @NotNull List<String> getAnotherObjectTargets(@NotNull String methodName);
    }
}

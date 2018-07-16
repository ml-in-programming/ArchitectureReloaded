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

            @Override
            public int numberOfCalls(@NotNull String callerName, @NotNull String targetName) {
                return 0;
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

        for (PsiMethod caller : repository.getMethods()) {
            String callerName = MethodUtils.calculateSignature(caller);

            for (PsiMethod target : repository.getMethods()) {
                String targetName = MethodUtils.calculateSignature(target);

                int expectedNumberOfCalls = validator.numberOfCalls(callerName, targetName);

                MethodInfo callerInfo =
                    repository.getMethodInfo(caller).orElseThrow(
                        () -> new IllegalStateException(
                            "Existing method doesn't have MethodInfo instance"
                        )
                    );

                MethodInfo targetInfo =
                    repository.getMethodInfo(target).orElseThrow(
                        () -> new IllegalStateException(
                            "Existing method doesn't have MethodInfo instance"
                        )
                    );

                assertEquals(expectedNumberOfCalls, (int) callerInfo.getInsideCalls(target));
                assertEquals(expectedNumberOfCalls, (int) targetInfo.getOutsideInvocations(caller));
            }
        }
    }

    private interface InfoValidator {
        @NotNull List<String> methodNames();

        int numberOfCalls(@NotNull String callerName, @NotNull String targetName);
    }
}

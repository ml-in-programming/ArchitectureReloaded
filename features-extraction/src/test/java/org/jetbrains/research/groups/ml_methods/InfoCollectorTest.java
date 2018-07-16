package org.jetbrains.research.groups.ml_methods;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiMethod;

public class InfoCollectorTest extends ScopeAbstractTest {
    public void testSingleMethod() throws Exception {
        AnalysisScope scope = createScope();
        MethodInfoRepository repository = InfoCollector.getInstance().collectInfo(scope);

        for (PsiMethod method : repository.getMethods()) {
            System.out.println(method.getName());
        }
    }
}

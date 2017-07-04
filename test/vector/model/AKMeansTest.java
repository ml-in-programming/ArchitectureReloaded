/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package vector.model;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class AKMeansTest extends LightCodeInsightFixtureTestCase {
    public void test() throws IOException {
        final VirtualFile file1 = myFixture.copyFileToProject("class_A.java");
        final VirtualFile file2 = myFixture.copyFileToProject("class_B.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        final PropertiesFinder properties = new PropertiesFinder();
        final PsiElementVisitor visitor = properties.createVisitor(analysisScope);
        analysisScope.accept(visitor);

        System.out.println(analysisScope.getDisplayName());
        System.out.println(myFixture.getProject().getBasePath());
        System.out.println();

        final MetricsRunImpl metricsRun = new MetricsRunImpl();
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        final MetricsProfile profile = repository.getProfileForName("Refactoring features");

        new MetricsExecutionContextImpl(project, analysisScope) {
            @Override
            public void onFinish() {
                metricsRun.setContext(analysisScope);

                final MetricsResult classMetrics = metricsRun.getResultsForCategory(MetricCategory.Class);
                final MetricsResult methodMetrics = metricsRun.getResultsForCategory(MetricCategory.Method);
                System.out.println("Classes: " + classMetrics.getMeasuredObjects().length);
                System.out.println("Methods: " + methodMetrics.getMeasuredObjects().length);

                ArrayList<Entity> entities = new ArrayList<>();

//              AKMeans alg5 = new AKMeans(entities, 50);
//              Map<String, String> refactorings5 = alg5.run();
            }
        }.execute(profile, metricsRun);
    }


    @Override
    protected String getTestDataPath() {
        return "src/vector/model/examples/example2";
    }
}
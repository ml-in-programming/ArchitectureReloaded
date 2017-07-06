/*
 *  Copyright 2017 Machine Learning Methods in Software Engineering Research Group
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
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import vector.model.entity.ClassEntity;
import vector.model.entity.Entity;
import vector.model.entity.FieldEntity;
import vector.model.entity.MethodEntity;

import java.io.IOException;
import java.util.*;

public class AKMeansTest extends LightCodeInsightFixtureTestCase {
    private Project project;
    private AnalysisScope analysisScope;
    private MetricsRunImpl metricsRun;
    private MetricsProfile profile;
    private PropertiesFinder properties;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        final VirtualFile file1 = myFixture.copyFileToProject("ClassA.java");
        final VirtualFile file2 = myFixture.copyFileToProject("ClassB.java");

        project = myFixture.getProject();
        analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        properties = new PropertiesFinder();
        analysisScope.accept(properties.createVisitor(analysisScope));

        metricsRun = new MetricsRunImpl();
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");
    }

    @Override
    protected String getTestDataPath() {
        return "resources/examples/example2";
    }

    public void test() throws IOException {
        new MetricsExecutionContextImpl(project, analysisScope) {
            @Override
            public void onFinish() {
                metricsRun.setContext(analysisScope);
                metricsRun.setProfileName(profile.getName());

                final MetricsResult classMetrics = metricsRun.getResultsForCategory(MetricCategory.Class);
                final MetricsResult methodMetrics = metricsRun.getResultsForCategory(MetricCategory.Method);

                final ArrayList<Entity> entities = new ArrayList<>();
                for (String obj : classMetrics.getMeasuredObjects()) {
                    if (obj.equals("null")) {
                        continue;
                    }
                    if (!properties.getAllClassesNames().contains(obj)) {
                        continue;
                    }
                    final Entity classEnt = new ClassEntity(obj, metricsRun, properties);
                    entities.add(classEnt);
                }
                for (String obj : methodMetrics.getMeasuredObjects()) {
                    if (obj.substring(0, obj.indexOf('.')).equals("null")) {
                        continue;
                    }
                    if (properties.hasElement(obj)) {
                        final Entity methodEnt = new MethodEntity(obj, metricsRun, properties);
                        entities.add(methodEnt);
                    }
                }

                final Set<String> fields = properties.getAllFields();
                for (String field : fields) {
                    final Entity fieldEnt = new FieldEntity(field, metricsRun, properties);
                    entities.add(fieldEnt);
                }

                System.out.println("Classes: " + classMetrics.getMeasuredObjects().length);
                System.out.println("Methods: " + methodMetrics.getMeasuredObjects().length);
                System.out.println("Properties: " + fields.size());

                assertEquals(2, classMetrics.getMeasuredObjects().length);
                assertEquals(6, methodMetrics.getMeasuredObjects().length);
                assertEquals(4, fields.size());

                Entity.normalize(entities);

                System.out.println("!!!\n");

                CCDA alg = new CCDA(entities);
                System.out.println("Starting CCDA...");
                System.out.println(alg.calculateQualityIndex());
                Map<String, String> refactorings = alg.run();
                System.out.println("Finished CCDA\n");
                for (String ent : refactorings.keySet()) {
                    System.out.println(ent + " --> " + refactorings.get(ent));
                }
                assertEquals(1, refactorings.size());
                assertEquals("ClassB.methodB1()", refactorings.keySet().toArray()[0]);
                assertEquals("ClassA", refactorings.get("ClassB.methodB1()"));

                MRI alg2 = new MRI(entities, properties.getAllClasses());
                System.out.println("\nStarting MMRI...");
                //alg2.printTableDistances();
                Map<String, String> refactorings2 = alg2.run();
                System.out.println("Finished MMRI");
                for (String method : refactorings2.keySet()) {
                    System.out.println(method + " --> " + refactorings2.get(method));
                }

                assertEquals(1, refactorings2.size());
                assertEquals("ClassB.methodB1()", refactorings2.keySet().toArray()[0]);
                assertEquals("ClassA", refactorings2.get("ClassB.methodB1()"));

                Set<String> common = new HashSet<String>(refactorings.keySet());
                common.retainAll(refactorings2.keySet());
                System.out.println("Common for ARI and CCDA: ");
                for (String move : common) {
                    System.out.print(move + " to ");
                    System.out.print(refactorings.get(move));
                    if (!refactorings2.get(move).equals(refactorings.get(move))) {
                        System.out.print(" vs " + refactorings2.get(move));
                    }
                    System.out.println();
                }
                System.out.println();

                assertEquals(new HashSet(Collections.singletonList("ClassB.methodB1()")), common);

                AKMeans alg5 = new AKMeans(entities, 50);
                System.out.println("\nStarting AKMeans...");
                Map<String, String> refactorings5 = alg5.run();
                System.out.println("Finished AKMeans");
                for (String method : refactorings5.keySet()) {
                    System.out.println(method + " --> " + refactorings5.get(method));
                }

                Set<String> refactoringsARIEC = new HashSet<>(refactorings5.keySet());
                refactoringsARIEC.retainAll(refactorings2.keySet());
                System.out.println("Common for ARI and EC: ");
                for (String move : refactoringsARIEC) {
                    System.out.print(move + " to ");
                    System.out.print(refactorings5.get(move));
                    if (!refactorings2.get(move).equals(refactorings5.get(move))) {
                        System.out.print(" vs " + refactorings2.get(move));
                    }
                    System.out.println();
                }
                System.out.println();

                // TODO: make AKMeans more deterministic somehow and get this assertion back
//                assertEquals(new HashSet(Collections.singletonList("ClassB.methodB1()")), refactoringsARIEC);

                HAC alg3 = new HAC(entities);
                System.out.println("\nStarting HAC...");
                refactorings = alg3.run();
                System.out.println("Finished HAC");
                for (String method : refactorings.keySet()) {
                    System.out.println(method + " --> " + refactorings.get(method));
                }

                final Map<String, String> expectedHAC = new HashMap<>();
                expectedHAC.put("ClassB.methodB1()", "ClassA");
                assertEquals(expectedHAC, refactorings);

                ARI alg4 = new ARI(entities);
                System.out.println("\nStarting ARI...");
                refactorings = alg4.run();
                System.out.println("Finished ARI");
                for (String method : refactorings.keySet()) {
                    System.out.println(method + " --> " + refactorings.get(method));
                }

                assertEquals(expectedHAC, refactorings);
            }
        }.execute(profile, metricsRun);
    }
}
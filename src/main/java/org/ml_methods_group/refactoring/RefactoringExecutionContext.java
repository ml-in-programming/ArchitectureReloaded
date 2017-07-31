/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.*;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.FieldEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.algorithm.properties.finder_strategy.FinderStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class RefactoringExecutionContext extends MetricsExecutionContextImpl {
    private static final String[] ALGORITHMS = {"ARI", "HAC", "CCDA", "MRI", "AKMeans"};

    @NotNull
    private final MetricsRunImpl metricsRun = new MetricsRunImpl();
    @NotNull
    private final MetricsProfile profile;
    @NotNull
    private final PropertiesFinder properties;
    @Nullable
    private final Consumer<RefactoringExecutionContext> continuation;
    private final List<Entity> entities = new ArrayList<>();
    private int classCount = 0;
    private int methodsCount = 0;
    private int fieldsCount = 0;

    public RefactoringExecutionContext(@NotNull Project project, @NotNull AnalysisScope scope,
                                       @NotNull MetricsProfile profile,
                                       @Nullable Consumer<RefactoringExecutionContext> continuation,
                                       @NotNull FinderStrategy strategy) {
        super(project, scope);
        this.profile = profile;
        this.continuation = continuation;

        properties = new PropertiesFinder(strategy);
        scope.accept(properties.createVisitor(scope));

        execute(profile, metricsRun);
    }

    @Override
    public void onFinish() {
        metricsRun.setProfileName(profile.getName());
        metricsRun.setContext(scope);
        metricsRun.setTimestamp(new TimeStamp());

        final MetricsResult classMetrics = metricsRun.getResultsForCategory(MetricCategory.Class);
        final MetricsResult methodMetrics = metricsRun.getResultsForCategory(MetricCategory.Method);

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

        // TODO: move fields processing to MetricsRunImpl
        final Set<String> fields = properties.getAllFields();
        for (String field : fields) {
            final Entity fieldEnt = new FieldEntity(field, metricsRun, properties);
            entities.add(fieldEnt);
        }

        Entity.normalize(entities);

        classCount = classMetrics.getMeasuredObjects().length;
        methodsCount = methodMetrics.getMeasuredObjects().length;
        fieldsCount = fields.size();

        System.out.println("Classes: " + classCount);
        System.out.println("Methods: " + methodsCount);
        System.out.println("Properties: " + fieldsCount);
        System.out.println();

        if (continuation != null) {
            continuation.accept(this);
        }
    }

    @NotNull
    private Map<String, String> calculateARI() {
        final ARI algorithm = new ARI(entities);
        System.out.println("\nStarting ARI...");
        final Map<String, String> refactorings = algorithm.run();
        System.out.println("Finished ARI");
        for (String method : refactorings.keySet()) {
            System.out.println(method + " --> " + refactorings.get(method));
        }
        return refactorings;
    }

    @NotNull
    private Map<String, String> calculateHAC() {
        final HAC algorithm = new HAC(entities);
        System.out.println("\nStarting HAC...");
        final Map<String, String> refactorings = algorithm.run();
        System.out.println("Finished HAC");
        for (String method : refactorings.keySet()) {
            System.out.println(method + " --> " + refactorings.get(method));
        }
        return refactorings;
    }

    @NotNull
    private Map<String, String> calculateAKMeans() {
        final AKMeans algorithm = new AKMeans(entities, 50);
        System.out.println("\nStarting AKMeans...");
        final Map<String, String> refactorings = algorithm.run();
        System.out.println("Finished AKMeans");
        for (String method : refactorings.keySet()) {
            System.out.println(method + " --> " + refactorings.get(method));
        }
        return refactorings;
    }

    @NotNull
    private Map<String, String> calculateMRI() {
        final MRI algorithm = new MRI(entities, properties.getAllClasses());
        System.out.println("\nStarting MMRI...");
        final Map<String, String> refactorings = algorithm.run();
        System.out.println("Finished MMRI");
        for (String method : refactorings.keySet()) {
            System.out.println(method + " --> " + refactorings.get(method));
        }
        return refactorings;
    }

    @NotNull
    private Map<String, String> calculateCCDA() {
        final CCDA algorithm = new CCDA(entities);
        System.out.println("Starting CCDA...");
        System.out.println(algorithm.calculateQualityIndex());
        final Map<String, String> refactorings = algorithm.run();
        System.out.println("Finished CCDA\n");
        for (String ent : refactorings.keySet()) {
            System.out.println(ent + " --> " + refactorings.get(ent));
        }
        return refactorings;
    }

    @NotNull
    public Map<String, String> calculateAlgorithmForName(String algorithm) {
        switch (algorithm) {
            case "ARI":
                return calculateARI();
            case "HAC":
                return calculateHAC();
            case "CCDA":
                return calculateCCDA();
            case "MRI":
                return calculateMRI();
            case "AKMeans":
                return calculateAKMeans();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }

    public int getClassCount() {
        return classCount;
    }

    public int getMethodsCount() {
        return methodsCount;
    }

    public int getFieldsCount() {
        return fieldsCount;
    }

    @NotNull
    public MetricsRunImpl getMetricsRun() {
        return metricsRun;
    }

    @NotNull
    public MetricsProfile getProfile() {
        return profile;
    }

    public static String[] getAvailableAlgorithms() {
        return ALGORITHMS.clone();
    }
}

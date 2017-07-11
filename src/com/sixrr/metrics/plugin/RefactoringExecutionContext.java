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

package com.sixrr.metrics.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vector.model.*;
import vector.model.entity.ClassEntity;
import vector.model.entity.Entity;
import vector.model.entity.FieldEntity;
import vector.model.entity.MethodEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class RefactoringExecutionContext extends MetricsExecutionContextImpl {
    @NotNull private final MetricsRunImpl metricsRun = new MetricsRunImpl();
    @NotNull private final MetricsProfile profile;
    @NotNull private final PropertiesFinder properties;
    @Nullable private final Consumer<RefactoringExecutionContext> continuation;
    private final boolean enableUi;
    private final List<Entity> entities = new ArrayList<>();
    private int classCount = 0;
    private int methodsCount = 0;
    private int fieldsCount = 0;

    public RefactoringExecutionContext(@NotNull Project project, @NotNull AnalysisScope scope
            , @NotNull MetricsProfile profile, boolean enableUi
            , @Nullable Consumer<RefactoringExecutionContext> continuation) {
        super(project, scope);
        this.profile = profile;
        this.enableUi = enableUi;
        this.continuation = continuation;

        properties = new PropertiesFinder();
        scope.accept(properties.createVisitor(scope));

        execute(profile, metricsRun);
    }

    public RefactoringExecutionContext(@NotNull Project project, @NotNull AnalysisScope scope
            , @NotNull MetricsProfile profile) {
        super(project, scope);
        this.profile = profile;
        enableUi = false;
        continuation = null;

        properties = new PropertiesFinder();
        scope.accept(properties.createVisitor(scope));

        executeSynchronously(profile, metricsRun);
    }

    private void executeSynchronously(final MetricsProfile profile, final MetricsResultsHolder resultsHolder) {
        calculateMetrics(profile, resultsHolder);
        onFinish();
    }

    @Override
    public void onFinish() {
        metricsRun.setProfileName(profile.getName());
        metricsRun.setContext(scope);
        metricsRun.setTimestamp(new TimeStamp());

        // TODO: move UI out of here. E.g. into AutomaticRefactoringAction
        if (enableUi) {
            final boolean showOnlyWarnings = MetricsReloadedConfig.getInstance().isShowOnlyWarnings();
            if(!metricsRun.hasWarnings(profile) && showOnlyWarnings) {
                ToolWindowManager.getInstance(project).notifyByBalloon(MetricsToolWindow.METRICS_TOOL_WINDOW_ID,
                        MessageType.INFO, MetricsReloadedBundle.message("no.metrics.warnings.found"));
                return;
            }

            final MetricsToolWindow toolWindow = MetricsToolWindow.getInstance(project);
            toolWindow.show(metricsRun, profile, scope, showOnlyWarnings);
        }

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
    public Map<String, String> calculateARI() {
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
    public Map<String, String> calculateHAC() {
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
    public Map<String, String> calculateAKMeans() {
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
    public Map<String, String> calculateMRI() {
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
    public Map<String, String> calculateCCDA() {
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

    public int getClassCount() {
        return classCount;
    }

    public int getMethodsCount() {
        return methodsCount;
    }

    public int getFieldsCount() {
        return fieldsCount;
    }


}

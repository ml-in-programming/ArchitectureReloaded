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

import java.util.*;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.metricModel.*;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.sixrr.metrics.MetricCategory;

import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;

import vector.model.*;
import vector.model.entity.ClassEntity;
import vector.model.entity.Entity;
import vector.model.entity.FieldEntity;
import vector.model.entity.MethodEntity;

import javax.swing.*;

public class AutomaticRefactoringAction extends BaseAnalysisAction{
    public AutomaticRefactoringAction() {
        super(MetricsReloadedBundle.message("metrics.calculation"), MetricsReloadedBundle.message("metrics"));
    }

    @Override
    protected void analyze(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        System.out.println(analysisScope.getDisplayName());
        System.out.println(project.getBasePath());
        System.out.println("");
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        final MetricsProfile profile = repository.getCurrentProfile();
        final MetricsToolWindow toolWindow = MetricsToolWindow.getInstance(project);
        final MetricsRunImpl metricsRun = new MetricsRunImpl();

        final PropertiesFinder properties = new PropertiesFinder();
        analysisScope.accept(properties.createVisitor(analysisScope));

        new MetricsExecutionContextImpl(project, analysisScope) {

            @Override
            public void onFinish() {
                final boolean showOnlyWarnings = MetricsReloadedConfig.getInstance().isShowOnlyWarnings();
                if(!metricsRun.hasWarnings(profile) && showOnlyWarnings) {
                    ToolWindowManager.getInstance(project).notifyByBalloon(MetricsToolWindow.METRICS_TOOL_WINDOW_ID,
                            MessageType.INFO, MetricsReloadedBundle.message("no.metrics.warnings.found"));
                    return;
                }
                final String profileName = profile.getName();
                metricsRun.setProfileName(profileName);
                metricsRun.setContext(analysisScope);
                metricsRun.setTimestamp(new TimeStamp());
                toolWindow.show(metricsRun, profile, analysisScope, showOnlyWarnings);

                MetricsResult classMetrics = metricsRun.getResultsForCategory(MetricCategory.Class);
                MetricsResult methodMetrics = metricsRun.getResultsForCategory(MetricCategory.Method);

                List<Entity> entities = new ArrayList<>();
                System.out.println("Classes: " + classMetrics.getMeasuredObjects().length);
                System.out.println("Methods: " + methodMetrics.getMeasuredObjects().length);
                for (String obj : classMetrics.getMeasuredObjects()) {
                    if (obj.equals("null")) {
                        continue;
                    }
                    if (!properties.getAllClassesNames().contains(obj)) {
                        continue;
                    }
                    Entity classEnt = new ClassEntity(obj, metricsRun, properties);
                    entities.add(classEnt);
                }
                for (String obj : methodMetrics.getMeasuredObjects()) {
                    if (obj.substring(0, obj.indexOf('.')).equals("null")) {
                        continue;
                    }
                    if (properties.hasElement(obj)) {
                        Entity methodEnt = new MethodEntity(obj, metricsRun, properties);
                        entities.add(methodEnt);
                    }
                }

                Set<String> fields = properties.getAllFields();
                System.out.println("Properties: " + fields.size());
                for (String field : fields) {
                    Entity fieldEnt = new FieldEntity(field, metricsRun, properties);
                    entities.add(fieldEnt);
                }

                /*for (Entity ent : entities) {
                    ent.print();
                    System.out.println();
                }*/
                System.out.println("!!!\n");

                Entity.normalize(entities);
                /*for (Entity ent : entities) {
                    ent.print();
                    System.out.println();
                }*/

                System.out.println("!!!\n");

                CCDA alg = new CCDA(entities);
                System.out.println("Starting CCDA...");
                System.out.println(alg.calculateQualityIndex());
                Map<String, String> refactorings = alg.run();
                System.out.println("Finished CCDA\n");
                for (String ent : refactorings.keySet()) {
                    System.out.println(ent + " --> " + refactorings.get(ent));
                }

                MRI alg2 = new MRI(entities, properties.getAllClasses());
                System.out.println("\nStarting MMRI...");
                //alg2.printTableDistances();
                Map<String, String> refactorings2 = alg2.run();
                System.out.println("Finished MMRI");
                for (String method : refactorings2.keySet()) {
                    System.out.println(method + " --> " + refactorings2.get(method));
                }

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


                HAC alg3 = new HAC(entities);
                System.out.println("\nStarting HAC...");
                refactorings = alg3.run();
                System.out.println("Finished HAC");
                for (String method : refactorings.keySet()) {
                    System.out.println(method + " --> " + refactorings.get(method));
                }

                ARI alg4 = new ARI(entities);
                System.out.println("\nStarting ARI...");
                refactorings = alg4.run();
                System.out.println("Finished ARI");
                for (String method : refactorings.keySet()) {
                    System.out.println(method + " --> " + refactorings.get(method));
                }



            }
        }.execute(profile, metricsRun);
    }

    @Override
    @Nullable
    protected JComponent getAdditionalActionSettings(Project project, BaseAnalysisActionDialog dialog) {
        return new ProfileSelectionPanel(project);
    }
}

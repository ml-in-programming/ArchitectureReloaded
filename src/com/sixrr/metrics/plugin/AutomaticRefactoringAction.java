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

package com.sixrr.metrics.plugin;

import java.lang.reflect.Array;
import java.util.*;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.metricModel.*;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindowManager;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vector.model.*;

import javax.swing.*;

/**
 * Created by Kivi on 02.04.2017.
 */
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
        final PsiElementVisitor visitor = properties.createVisitor(analysisScope);
        ProgressManager.getInstance().runProcess(new Runnable() {
            @Override
            public void run() {
                analysisScope.accept(visitor);;
            }
        }, new EmptyProgressIndicator());

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

                ArrayList<Entity> entities = new ArrayList<Entity>();
                System.out.println("Classes: " + classMetrics.getMeasuredObjects().length);
                System.out.println("Methods: " + methodMetrics.getMeasuredObjects().length);
                for (String obj : classMetrics.getMeasuredObjects()) {
                    if (obj.equals("null")) {
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
                refactorings = alg2.run();
                System.out.println("Finished MMRI");
                for (String method : refactorings.keySet()) {
                    System.out.println(method + " --> " + refactorings.get(method));
                }


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

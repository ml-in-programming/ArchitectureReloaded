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

package org.ml_methods_group.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.dialogs.ProfileSelectionPanel;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.sddrar.SDDRARFacade;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import javax.swing.*;

public class SDDRARTrainAction extends BaseAnalysisAction {
    protected SDDRARTrainAction() {
        super(ArchitectureReloadedBundle.message("sddrar.learn.title"), ArchitectureReloadedBundle.message("sddrar"));
    }

    @Override
    protected void analyze(@NotNull Project project, @NotNull AnalysisScope scope) {
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        final MetricsProfile profile = repository.getCurrentProfile();
        final MetricsToolWindow toolWindow = MetricsToolWindow.getInstance(project);
        final MetricsRunImpl metricsRun = new MetricsRunImpl();
        new MetricsExecutionContextImpl(project, scope) {

            @Override
            public void onFinish() {
                final boolean showOnlyWarnings = MetricsReloadedConfig.getInstance().isShowOnlyWarnings();
                final String profileName = profile.getName();
                metricsRun.setProfileName(profileName);
                metricsRun.setContext(scope);
                metricsRun.setTimestamp(new TimeStamp());
                SDDRARFacade.trainAndPersistModel(metricsRun);
                toolWindow.show(metricsRun, profile, scope, showOnlyWarnings);
            }
        }.execute(profile, metricsRun);
    }

    @Override
    @Nullable
    protected JComponent getAdditionalActionSettings(Project project, BaseAnalysisActionDialog dialog) {
        return new ProfileSelectionPanel(project);
    }
}

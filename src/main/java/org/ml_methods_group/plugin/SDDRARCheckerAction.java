/*
 * Copyright 2005-2016 Sixth and Red River Software, Bas Leijdekkers
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

package org.ml_methods_group.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.config.MetricsReloadedConfig;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.ui.metricdisplay.MetricsToolWindow;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.sddrar.SDDRARFacade;
import org.ml_methods_group.utils.ArchitectureReloadedBundle;

import java.util.List;

public class SDDRARCheckerAction extends BaseAnalysisAction {

    public SDDRARCheckerAction() {
        super(ArchitectureReloadedBundle.message("sddrar.check.title"), ArchitectureReloadedBundle.message("sddrar"));
    }

    @Override
    protected void analyze(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        final MetricsProfile profile = repository.getProfileForName(StockMetricsBundle.message("sddrar.temp.profile.name"));
        SDDRARFacade.selectInterestingMetrics(profile);
        final MetricsToolWindow toolWindow = MetricsToolWindow.getInstance(project);
        final MetricsRunImpl metricsRun = new MetricsRunImpl();
        new MetricsExecutionContextImpl(project, analysisScope) {

            @Override
            public void onFinish() {
                final boolean showOnlyWarnings = MetricsReloadedConfig.getInstance().isShowOnlyWarnings();
                final String profileName = profile.getName();
                metricsRun.setProfileName(profileName);
                metricsRun.setContext(analysisScope);
                metricsRun.setTimestamp(new TimeStamp());
                List<String> faulty = SDDRARFacade.checkNewData(metricsRun);
                metricsRun.leaveFaultyClasses(faulty);
                toolWindow.show(metricsRun, profile, analysisScope, showOnlyWarnings);
            }
        }.execute(profile, metricsRun);

    }
}

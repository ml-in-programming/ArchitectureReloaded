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

package com.sixrr.metrics;

import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * To add com.sixrr.metrics to IntelliJ IDEA, you first create the Metric classes for them, and then bind them into IDEA using a
 * MetricsProvider component.  Create an implementation of MetricsProvider which lists the com.sixrr.metrics you wish to add.
 * Additionally, you can specify a list of pre-built com.sixrr.metrics profiles, which may make it easier for new users to use your
 * com.sixrr.metrics.
 */
public interface MetricProvider {

    ExtensionPointName<MetricProvider> EXTENSION_POINT_NAME =
            ExtensionPointName.create("ArchitectureReloaded.metricProvider");

    /**
     * Returns the list of com.sixrr.metrics provided by this provider.
     * @return the com.sixrr.metrics for this provider
     */
    @NotNull
    List<Metric> getMetrics();

    /**
     * Returns the list of prebuilt com.sixrr.metrics profiles provided by this provider.
     * @return the prebuilt com.sixrr.metrics profiles for this provider.
     */
    @NotNull
    List<PrebuiltMetricProfile> getPrebuiltProfiles();
}

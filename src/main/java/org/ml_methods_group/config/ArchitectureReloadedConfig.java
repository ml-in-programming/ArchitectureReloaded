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

package org.ml_methods_group.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@State(name = "ArchitectureReloaded", storages = @Storage(file = "architecture.reloaded.xml"))
public final class ArchitectureReloadedConfig implements PersistentStateComponent<ArchitectureReloadedConfig> {

    public Set<String> selectedAlgorithms = new HashSet<>();

    private ArchitectureReloadedConfig() {}

    public static ArchitectureReloadedConfig getInstance() {
        return ServiceManager.getService(ArchitectureReloadedConfig.class);
    }

    public Set<String> getSelectedAlgorithms() {
        return Collections.unmodifiableSet(selectedAlgorithms);
    }

    public void setSelected(String algorithm, boolean isSelected) {
        if (isSelected) {
            selectedAlgorithms.add(algorithm);
        } else {
            selectedAlgorithms.remove(algorithm);
        }
    }

    @Nullable
    @Override
    public ArchitectureReloadedConfig getState() {
        return this;
    }

    @Override
    public void loadState(ArchitectureReloadedConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}

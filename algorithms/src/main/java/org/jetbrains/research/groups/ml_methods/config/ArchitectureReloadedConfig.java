package org.jetbrains.research.groups.ml_methods.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@State(name = "ArchitectureReloaded", storages = @Storage(file = "architecture.reloaded.xml"))
public final class ArchitectureReloadedConfig implements PersistentStateComponent<ArchitectureReloadedConfig> {
    private final Set<Algorithm> selectedAlgorithms =
            new HashSet<>(AlgorithmsRepository.getAvailableAlgorithms());
    private boolean enableFieldRefactoring = false;

    private ArchitectureReloadedConfig() {}

    public static ArchitectureReloadedConfig getInstance() {
        return ServiceManager.getService(ArchitectureReloadedConfig.class);
    }

    public Set<Algorithm> getSelectedAlgorithms() {
        return Collections.unmodifiableSet(selectedAlgorithms);
    }

    public void setSelected(Algorithm algorithm, boolean isSelected) {
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

    public void setEnableFieldRefactoring(boolean enableFieldRefactoring) {
        this.enableFieldRefactoring = enableFieldRefactoring;
    }

    @Contract(pure = true)
    public boolean enableFieldRefactoring() {
        return enableFieldRefactoring;
    }
}

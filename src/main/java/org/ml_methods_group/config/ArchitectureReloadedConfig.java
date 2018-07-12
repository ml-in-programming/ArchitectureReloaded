package org.ml_methods_group.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.Algorithm;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@State(name = "ArchitectureReloaded", storages = @Storage(file = "architecture.reloaded.xml"))
public final class ArchitectureReloadedConfig implements PersistentStateComponent<ArchitectureReloadedConfig> {

    private final Set<Algorithm> selectedAlgorithms =
            new HashSet<>(Arrays.asList(RefactoringExecutionContext.getAvailableAlgorithms()));
    private boolean isFieldRefactoringAvailable = false;

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

    public void setFieldRefactoringsAvailable() {
        isFieldRefactoringAvailable = !isFieldRefactoringAvailable;
    }

    public boolean isFieldRefactoringAvailable() {
        return isFieldRefactoringAvailable;
    }
}

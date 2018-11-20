package org.jetbrains.research.groups.ml_methods.vectorization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JBRefactoringTextRepresentation;

public class DlbRefactoringVector extends DlbVector {
    private final @NotNull
    JBRefactoringTextRepresentation refactoring;

    public DlbRefactoringVector(@NotNull JBRefactoringTextRepresentation refactoring,
                                double distanceWithSourceClass, double distanceWithTargetClass) {
        super(refactoring.getMethodName(),
                getSimpleName(refactoring.getSourceClassQualifiedName()),
                getSimpleName(refactoring.getTargetClassQualifiedName()),
                distanceWithSourceClass, distanceWithTargetClass);
        this.refactoring = refactoring;
    }

    private static String getSimpleName(@NotNull String qualifiedName) {
        String[] names = qualifiedName.split("\\.");
        return names[names.length - 1];
    }
}

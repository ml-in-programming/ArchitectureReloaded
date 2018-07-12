package org.ml_methods_group.refactoring;

import org.apache.log4j.or.ObjectRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * Log4J renderer for {@link RefactoringSessionInfo}. This renderer converts
 * {@link RefactoringSessionInfo} to a {@link String} that will be written to a log.
 */
public class RefactoringSessionInfoRenderer implements ObjectRenderer {
    /**
     * Converts given object which is expected to be a {@link RefactoringSessionInfo} to a
     * {@link String} representation.
     *
     * @param obj an arbitrary object which is expected to be a {@link RefactoringSessionInfo}.
     * @return string that will be written to a log.
     * @throws IllegalArgumentException if given object is not a {@link RefactoringSessionInfo}.
     */
    @Override
    public @NotNull String doRender(final @NotNull Object obj) {
        if (!(obj instanceof RefactoringSessionInfo)) {
            throw new IllegalArgumentException(
                "RefactoringSessionInfoRenderer received not a RefactoringSessionInfo as an argument!"
            );
        }

        RefactoringSessionInfo info = (RefactoringSessionInfo) obj;

        final String lineSeparator = System.getProperty("line.separator");

        StringBuilder builder = new StringBuilder();

        builder.append("Accepted refactorings").append(lineSeparator);
        for (RefactoringFeatures features : info.getAcceptedRefactoringsFeatures()) {
            builder.append(serializeFeatures(features)).append(lineSeparator);
        }

        builder.append(lineSeparator);

        builder.append("Rejected refactorings").append(lineSeparator);
        for (RefactoringFeatures features : info.getRejectedRefactoringsFeatures()) {
            builder.append(serializeFeatures(features)).append(lineSeparator);
        }

        return builder.toString();
    }

    private @NotNull String serializeFeatures(final @NotNull RefactoringFeatures features) {
        if (features.isFieldMove()) {
            return "[1]";
        } else {
            return "[0]";
        }
    }
}

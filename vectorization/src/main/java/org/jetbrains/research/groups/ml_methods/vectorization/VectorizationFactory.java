package org.jetbrains.research.groups.ml_methods.vectorization;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VectorizationFactory {
    @NotNull
    public static Optional<Vectorization> parseType(@NotNull String type) {
        switch (type) {
            case "DLB":
                return Optional.of(new DlbVectorization());
        }
        return Optional.empty();
    }
}

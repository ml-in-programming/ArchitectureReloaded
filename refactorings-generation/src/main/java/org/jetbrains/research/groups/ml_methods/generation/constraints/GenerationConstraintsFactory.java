package org.jetbrains.research.groups.ml_methods.generation.constraints;

import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraintsFactory.GenerationConstraintType.*;

public class GenerationConstraintsFactory {
    public enum GenerationConstraintType {
        ACCEPT_ANY, ACCEPT_METHOD_PARAMS
    }

    private static final Map<GenerationConstraintType, GenerationConstraint> CONSTRAINTS = new HashMap<>();

    static {
        CONSTRAINTS.put(ACCEPT_ANY, new AcceptAnyGenerationConstraint());
        CONSTRAINTS.put(ACCEPT_METHOD_PARAMS, new AcceptMethodParamsGenerationConstraint());
    }

    public static GenerationConstraint get(GenerationConstraintType constraint) {
        return CONSTRAINTS.get(constraint);
    }
}

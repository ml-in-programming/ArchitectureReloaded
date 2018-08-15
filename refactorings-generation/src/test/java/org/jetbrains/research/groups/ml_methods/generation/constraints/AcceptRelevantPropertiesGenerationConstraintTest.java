package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.analysis.AnalysisScope;

import java.util.HashMap;
import java.util.Map;

public class AcceptRelevantPropertiesGenerationConstraintTest extends AbstractGenerationConstraintTest {
    private static final GenerationConstraint CONSTRAINT = GenerationConstraintsFactory.get(GenerationConstraintsFactory.GenerationConstraintType.ACCEPT_RELEVANT_PROPERTIES);
    private static final Map<MethodTestRepresentation, Boolean> methodResults = new HashMap<>();
    private static final Map<ClassTestRepresentation, Boolean> classResults = new HashMap<>();
    private static final Map<RefactoringTestRepresentation, Boolean> refactoringResults = new HashMap<>();
    private AnalysisScope scope;
    private static final String packageName = "acceptRelevantPropertiesGenerationConstraint";

    static {
        methodResults.put(new MethodTestRepresentation(packageName + ".A", "move1"), true);
        methodResults.put(new MethodTestRepresentation(packageName + ".A", "move2"), true);
        methodResults.put(new MethodTestRepresentation(packageName + ".A", "notMove1"), false);
        methodResults.put(new MethodTestRepresentation(packageName + ".A", "notMove2"), false);
        methodResults.put(new MethodTestRepresentation(packageName + ".B", "b1"), false);
        methodResults.put(new MethodTestRepresentation(packageName + ".B", "b2"), false);

        classResults.put(new ClassTestRepresentation(packageName + ".A"), true);
        classResults.put(new ClassTestRepresentation(packageName + ".B"), true);

        refactoringResults.put(new RefactoringTestRepresentation(packageName + ".A", "move1", packageName + ".B"), true);
        refactoringResults.put(new RefactoringTestRepresentation(packageName + ".A", "move2", packageName + ".B"), true);
        refactoringResults.put(new RefactoringTestRepresentation(packageName + ".A", "move1", packageName + ".A"), false);
        refactoringResults.put(new RefactoringTestRepresentation(packageName + ".A", "move2", packageName + ".A"), false);
    }

    public void testAcceptRelevantPropertiesGenerationConstraint() {
        scope = createScope("A.java", "B.java");
        executeTest();
    }

    @Override
    AnalysisScope getScope() {
        return scope;
    }

    @Override
    Map<MethodTestRepresentation, Boolean> getExpectedMethodResults() {
        return methodResults;
    }

    @Override
    Map<ClassTestRepresentation, Boolean> getExpectedClassResults() {
        return classResults;
    }

    @Override
    Map<RefactoringTestRepresentation, Boolean> getExpectedRefactoringResults() {
        return refactoringResults;
    }

    @Override
    GenerationConstraint getTestingConstraint() {
        return CONSTRAINT;
    }
}
package org.jetbrains.research.groups.ml_methods.extraction.features.vector;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.AnotherInstanceCallers;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameInstanceCallers;
import org.jetbrains.research.groups.ml_methods.extraction.features.TargetClassCallers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class VectorSerializerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void serializationDeserialization() throws Exception {
        File folder1 = temporaryFolder.newFolder();
        File folder2 = temporaryFolder.newFolder();

        AnotherInstanceCallers f1 = new AnotherInstanceCallers(1);
        TargetClassCallers f2 = new TargetClassCallers(2.5);
        SameInstanceCallers f3 = new SameInstanceCallers(0.2);

        FeatureVector v1 = new FeatureVector(Arrays.asList(f1, f2));
        FeatureVector v2 = new FeatureVector(Arrays.asList(f2, f3));

        VectorSerializer.getInstance().serialize(
            Collections.singletonList(v1),
            folder1.toPath()
        );

        VectorSerializer.getInstance().serialize(
            Collections.singletonList(v2),
            folder2.toPath()
        );

        List<FeatureVector> deserializedVectors =
            VectorSerializer.getInstance().deserialize(temporaryFolder.getRoot().toPath());

        assertThat(deserializedVectors, containsInAnyOrder(vector(v1), vector(v2)));
    }

    private static FeatureVectorMatcher vector(final @NotNull FeatureVector featureVector) {
        return new FeatureVectorMatcher(featureVector);
    }

    private static class FeatureVectorMatcher extends TypeSafeMatcher<FeatureVector> {
        private final @NotNull FeatureVector expected;

        public FeatureVectorMatcher(final @NotNull FeatureVector expected) {
            this.expected = expected;
        }

        @Override
        protected boolean matchesSafely(final FeatureVector actual) {
            if (actual == null) {
                return false;
            }

            List<Feature> expectedFeatures = expected.getComponents();
            List<Feature> actualFeatures = actual.getComponents();

            if (expectedFeatures.size() != actualFeatures.size()) {
                return false;
            }

            int commonSize = expectedFeatures.size();
            for (int i = 0; i < commonSize; i++) {
                if (!matches(expectedFeatures.get(i), actualFeatures.get(i))) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void describeTo(final @NotNull Description description) {
            description.appendText("feature vector: ");

            description.appendText(expected.getComponents().stream().map(it ->
                it.getClass().getSimpleName() +
                " = " +
                Double.toString(it.getValue())
            ).collect(Collectors.joining(", ")));
        }

        private boolean matches(final @NotNull Feature expected, final @NotNull Feature actual) {
            return expected.getClass().equals(actual.getClass()) &&
                    expected.getValue() == actual.getValue();
        }
    }
}
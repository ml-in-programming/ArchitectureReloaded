/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.algorithm.entity;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RmmrEntitySearcherTest extends LightCodeInsightFixtureTestCase {
    private EntitySearchResult searchResult;

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testCases/" + getPackage();
    }

    @NotNull
    @Contract(pure = true)
    private String getPackage() {
        return "movieRentalStoreWithFeatureEnvy";
    }

    public void testAnalyze() {
        final VirtualFile customer = myFixture.copyFileToProject("Customer.java");
        final VirtualFile movie = myFixture.copyFileToProject("Movie.java");
        final VirtualFile rental = myFixture.copyFileToProject("Rental.java");

        AnalysisScope analysisScope = new AnalysisScope(myFixture.getProject(), Arrays.asList(customer, movie, rental));
        searchResult = RmmrEntitySearcher.analyze(analysisScope);

        assertEquals(3, searchResult.getClasses().size());
        // TODO: tests tightly depend on RMMR configs, but there is a lot of possible configurations. Rewrite or leave only one config.
        // checkCustomer();
        // checkMovie();
        // checkRental();
    }

    private void checkCustomer() {
        Map<String, Set<String>> expectedConceptualSets = new HashMap<>();
        expectedConceptualSets.put("Customer.getMovie(Movie)", new HashSet<>(Arrays.asList("Movie", "Rental")));
        expectedConceptualSets.put("Customer.addRental(Rental)", new HashSet<>(Collections.singletonList("Customer")));
        expectedConceptualSets.put("Customer.getName()", new HashSet<>(Collections.singletonList("Customer")));
        checkConceptualSetForClass("Customer", expectedConceptualSets);
    }

    private void checkMovie() {
        Map<String, Set<String>> expectedConceptualSets = new HashMap<>();
        expectedConceptualSets.put("Movie.getPriceCode()", new HashSet<>(Collections.singletonList("Movie")));
        expectedConceptualSets.put("Movie.setPriceCode(int)", new HashSet<>(Collections.singletonList("Movie")));
        expectedConceptualSets.put("Movie.getTitle()", new HashSet<>(Collections.singletonList("Movie")));
        checkConceptualSetForClass("Movie", expectedConceptualSets);
    }

    private void checkRental() {
        Map<String, Set<String>> expectedConceptualSets = new HashMap<>();
        expectedConceptualSets.put("Rental.getDaysRented()", new HashSet<>(Collections.singletonList("Rental")));
        checkConceptualSetForClass("Rental", expectedConceptualSets);
    }

    private void checkConceptualSetForClass(String className, Map<String, Set<String>> expectedConceptualSets) {
        searchResult.getMethods().forEach(methodEntity -> {
            if (methodEntity.getClassName().equals(className)) {
                assertTrue(expectedConceptualSets.containsKey(methodEntity.getName()));

                Set<String> conceptualSet = methodEntity.getRelevantProperties().getClasses();
                Set<String> expectedConceptualSet = expectedConceptualSets.get(methodEntity.getName());
                assertEquals(expectedConceptualSet, conceptualSet);
            }
        });
    }
}
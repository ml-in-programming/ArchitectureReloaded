package org.ml_methods_group.algorithm.entity;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.util.*;

public class RmmrEntitySearcherTest extends LightCodeInsightFixtureTestCase {
    private EntitySearchResult searchResult;

    @Override
    protected String getTestDataPath() {
        return "testdata/moveMethod/movieRentalStore";
    }

    public void testAnalyze() throws Exception {
        final VirtualFile customer = myFixture.copyFileToProject("Customer.java");
        final VirtualFile movie = myFixture.copyFileToProject("Movie.java");
        final VirtualFile rental = myFixture.copyFileToProject("Rental.java");

        AnalysisScope analysisScope = new AnalysisScope(myFixture.getProject(), Arrays.asList(customer, movie, rental));
        searchResult = RmmrEntitySearcher.analyze(analysisScope);

        checkCustomer();
        checkMovie();
        checkRental();
    }

    private void checkCustomer() {
        Map<String, Set<String>> expectedConceptualSets = new HashMap<>();
        expectedConceptualSets.put("Customer.getMovie(Movie)", new HashSet<>(Arrays.asList("Movie", "Rental")));
        expectedConceptualSets.put("Customer.addRental(Rental)", new HashSet<>(Collections.singletonList("Customer")));
        expectedConceptualSets.put("Customer.getName()", new HashSet<>(Collections.singletonList("Customer")));
        checkConceptualSetForClass("Customer", expectedConceptualSets);
    }

    private void checkMovie() throws Exception {
        Map<String, Set<String>> expectedConceptualSets = new HashMap<>();
        expectedConceptualSets.put("Movie.getPriceCode()", new HashSet<>(Collections.singletonList("Movie")));
        expectedConceptualSets.put("Movie.setPriceCode(int)", new HashSet<>(Collections.singletonList("Movie")));
        expectedConceptualSets.put("Movie.getTitle()", new HashSet<>(Collections.singletonList("Movie")));
        checkConceptualSetForClass("Movie", expectedConceptualSets);
    }

    private void checkRental() throws Exception {
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
package org.ml_methods_group.algorithm;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.algorithm.entity.RmmrEntitySearcher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class RMMRTest extends LightCodeInsightFixtureTestCase {
    private EntitySearchResult searchResult;
    private RMMR algorithm = new RMMR();
    private Method getDistanceWithMethod;
    private Method getDistanceWithClass;

    @Override
    protected String getTestDataPath() {
        return "testdata/moveMethod/movieRentalStore";
    }

    private void init() throws NoSuchMethodException {
        final VirtualFile customer = myFixture.copyFileToProject("Customer.java");
        final VirtualFile movie = myFixture.copyFileToProject("Movie.java");
        final VirtualFile rental = myFixture.copyFileToProject("Rental.java");

        AnalysisScope analysisScope = new AnalysisScope(myFixture.getProject(), Arrays.asList(customer, movie, rental));
        searchResult = RmmrEntitySearcher.analyze(analysisScope);
        getDistanceWithMethod = RMMR.class.getDeclaredMethod("getDistance",
                MethodEntity.class, MethodEntity.class);
        getDistanceWithMethod.setAccessible(true);
        getDistanceWithClass = RMMR.class.getDeclaredMethod("getDistance",
                MethodEntity.class, ClassEntity.class);
        getDistanceWithClass.setAccessible(true);
    }

    private void setUpMethodsByClass() throws NoSuchFieldException, IllegalAccessException {
        Field methodsByClass = RMMR.class.getDeclaredField("methodsByClass");
        methodsByClass.setAccessible(true);
        Map<ClassEntity, Set<MethodEntity>> methodsByClassToSet = new HashMap<>();
        searchResult.getMethods().forEach(methodEntity -> {
            List<ClassEntity> methodClassEntity = searchResult.getClasses().stream()
                    .filter(classEntity -> methodEntity.getClassName().equals(classEntity.getName()))
                    .collect(Collectors.toList());
            methodsByClassToSet.computeIfAbsent(methodClassEntity.get(0), anyKey -> new HashSet<>()).add(methodEntity);
        });
        methodsByClass.set(algorithm, methodsByClassToSet);
    }

    public void testDistanceBetweenMethods() throws Exception {
        init();
        checkGetMovieBetweenMethods();
        checkAddRentalBetweenMethods();
        checkGetDaysRentedBetweenMethods();
        checkSetPriceCodeBetweenMethods();
    }

    public void testDistanceBetweenMethodAndClass() throws Exception {
        init();
        setUpMethodsByClass();
        checkGetMovieWithClasses();
        checkAddRentalWithClasses();
        checkGetDaysRentedWithClasses();
        checkSetPriceCodeWithClasses();
    }

    private void checkSetPriceCodeWithClasses() throws InvocationTargetException, IllegalAccessException {
        String methodName = "Movie.setPriceCode(int)";

        double expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Customer.getMovie(Movie)");
        expected += runGetDistanceWithMethod(methodName, "Customer.addRental(Rental)");
        expected += runGetDistanceWithMethod(methodName, "Customer.getName()");
        expected /= 3;
        checkGetDistanceWithClass(methodName, "Customer", expected);

        expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Movie.getPriceCode()");
        expected += runGetDistanceWithMethod(methodName, "Movie.getTitle()");
        expected /= 2;
        checkGetDistanceWithClass(methodName, "Movie", expected);

        expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Rental.getDaysRented()");
        expected /= 1;
        checkGetDistanceWithClass(methodName, "Rental", expected);
    }

    private void checkGetDaysRentedWithClasses() throws InvocationTargetException, IllegalAccessException {
        String methodName = "Rental.getDaysRented()";

        double expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Customer.getMovie(Movie)");
        expected += runGetDistanceWithMethod(methodName, "Customer.addRental(Rental)");
        expected += runGetDistanceWithMethod(methodName, "Customer.getName()");
        expected /= 3;
        checkGetDistanceWithClass(methodName, "Customer", expected);

        expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Movie.getPriceCode()");
        expected += runGetDistanceWithMethod(methodName, "Movie.setPriceCode(int)");
        expected += runGetDistanceWithMethod(methodName, "Movie.getTitle()");
        expected /= 3;
        checkGetDistanceWithClass(methodName, "Movie", expected);

        expected = 1;
        checkGetDistanceWithClass(methodName, "Rental", expected);
    }

    private void checkAddRentalWithClasses() throws InvocationTargetException, IllegalAccessException {
        String methodName = "Customer.addRental(Rental)";

        double expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Customer.getMovie(Movie)");
        expected += runGetDistanceWithMethod(methodName, "Customer.getName()");
        expected /= 2;
        checkGetDistanceWithClass(methodName, "Customer", expected);

        expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Movie.getPriceCode()");
        expected += runGetDistanceWithMethod(methodName, "Movie.setPriceCode(int)");
        expected += runGetDistanceWithMethod(methodName, "Movie.getTitle()");
        expected /= 3;
        checkGetDistanceWithClass(methodName, "Movie", expected);

        expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Rental.getDaysRented()");
        expected /= 1;
        checkGetDistanceWithClass(methodName, "Rental", expected);
    }

    private void checkGetMovieWithClasses() throws InvocationTargetException, IllegalAccessException {
        String methodName = "Customer.getMovie(Movie)";

        double expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Customer.addRental(Rental)");
        expected += runGetDistanceWithMethod(methodName, "Customer.getName()");
        expected /= 2;
        checkGetDistanceWithClass(methodName, "Customer", expected);

        expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Movie.getPriceCode()");
        expected += runGetDistanceWithMethod(methodName, "Movie.setPriceCode(int)");
        expected += runGetDistanceWithMethod(methodName, "Movie.getTitle()");
        expected /= 3;
        checkGetDistanceWithClass(methodName, "Movie", expected);

        expected = 0;
        expected += runGetDistanceWithMethod(methodName, "Rental.getDaysRented()");
        expected /= 1;
        checkGetDistanceWithClass(methodName, "Rental", expected);
    }

    private void checkSetPriceCodeBetweenMethods() throws InvocationTargetException, IllegalAccessException {
        String methodName = "Movie.setPriceCode(int)";

        checkGetDistanceWithMethod(methodName, "Customer.getMovie(Movie)", 1 - 1 / 2.0);
        checkGetDistanceWithMethod(methodName, "Customer.addRental(Rental)", 1 - 0 / 2.0);
        checkGetDistanceWithMethod(methodName, "Customer.getName()", 1 - 0 / 2.0);

        checkGetDistanceWithMethod(methodName, "Movie.getPriceCode()", 1 - 1 / 1.0);
        checkGetDistanceWithMethod(methodName, "Movie.setPriceCode(int)", 1 - 1 / 1.0);
        checkGetDistanceWithMethod(methodName, "Movie.getTitle()", 1 - 1 / 1.0);

        checkGetDistanceWithMethod(methodName, "Rental.getDaysRented()", 1 - 0 / 2.0);
    }

    private void checkGetDaysRentedBetweenMethods() throws InvocationTargetException, IllegalAccessException {
        String methodName = "Rental.getDaysRented()";

        checkGetDistanceWithMethod(methodName, "Customer.getMovie(Movie)", 1 - 1 / 2.0);
        checkGetDistanceWithMethod(methodName, "Customer.addRental(Rental)", 1 - 0 / 2.0);
        checkGetDistanceWithMethod(methodName, "Customer.getName()", 1 - 0 / 2.0);

        checkGetDistanceWithMethod(methodName, "Movie.getPriceCode()", 1 - 0 / 2.0);
        checkGetDistanceWithMethod(methodName, "Movie.setPriceCode(int)", 1 - 0 / 2.0);
        checkGetDistanceWithMethod(methodName, "Movie.getTitle()", 1 - 0 / 2.0);

        checkGetDistanceWithMethod(methodName, "Rental.getDaysRented()", 1 - 1 / 1.0);
    }

    private void checkAddRentalBetweenMethods() throws InvocationTargetException, IllegalAccessException {
        String methodName = "Customer.addRental(Rental)";

        checkGetDistanceWithMethod(methodName, "Customer.getMovie(Movie)", 1 - 0 / 3.0);
        checkGetDistanceWithMethod(methodName, "Customer.addRental(Rental)", 1 - 1 / 1.0);
        checkGetDistanceWithMethod(methodName, "Customer.getName()", 1 - 1 / 1.0);

        checkGetDistanceWithMethod(methodName, "Movie.getPriceCode()", 1 - 0 / 2.0);
        checkGetDistanceWithMethod(methodName, "Movie.setPriceCode(int)", 1 - 0 / 2.0);
        checkGetDistanceWithMethod(methodName, "Movie.getTitle()", 1 - 0 / 2.0);

        checkGetDistanceWithMethod(methodName, "Rental.getDaysRented()", 1 - 0 / 2.0);
    }

    private void checkGetMovieBetweenMethods() throws InvocationTargetException, IllegalAccessException {
        String methodName = "Customer.getMovie(Movie)";

        checkGetDistanceWithMethod(methodName, "Customer.getMovie(Movie)", 1 - 2 / 2.0);
        checkGetDistanceWithMethod(methodName, "Customer.addRental(Rental)", 1 - 0 / 3.0);
        checkGetDistanceWithMethod(methodName, "Customer.getName()", 1 - 0 / 3.0);

        checkGetDistanceWithMethod(methodName, "Movie.getPriceCode()", 1 - 1 / 2.0);
        checkGetDistanceWithMethod(methodName, "Movie.setPriceCode(int)", 1 - 1 / 2.0);
        checkGetDistanceWithMethod(methodName, "Movie.getTitle()", 1 - 1 / 2.0);

        checkGetDistanceWithMethod(methodName, "Rental.getDaysRented()", 1 - 1 / 2.0);
    }

    private void checkGetDistanceWithMethod(String methodName1, String methodName2, double expected) throws InvocationTargetException, IllegalAccessException {
        assertEquals(expected, runGetDistanceWithMethod(methodName1, methodName2));
    }

    private Double runGetDistanceWithMethod(String methodName1, String methodName2) throws InvocationTargetException, IllegalAccessException {
        MethodEntity methodEntity1 = searchResult.getMethods().stream().
                filter(methodEntity -> methodEntity.getName().equals(methodName1)).
                findAny().orElseThrow(NoSuchElementException::new);
        MethodEntity methodEntity2 = searchResult.getMethods().stream().
                filter(methodEntity -> methodEntity.getName().equals(methodName2)).
                findAny().orElseThrow(NoSuchElementException::new);
        return (Double) getDistanceWithMethod.invoke(algorithm, methodEntity1, methodEntity2);
    }

    private void checkGetDistanceWithClass(String methodName, String className, double expected) throws InvocationTargetException, IllegalAccessException {
        MethodEntity methodEntity = searchResult.getMethods().stream().
                filter(methodEntity2 -> methodEntity2.getName().equals(methodName)).
                findAny().orElseThrow(NoSuchElementException::new);
        ClassEntity classEntity = searchResult.getClasses().stream().
                filter(classEntity2 -> classEntity2.getName().equals(className)).
                findAny().orElseThrow(NoSuchElementException::new);
        assertEquals(expected, getDistanceWithClass.invoke(algorithm, methodEntity, classEntity));
    }
}
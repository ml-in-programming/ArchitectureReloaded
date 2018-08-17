package com.sixrr.stockmetrics;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPackage;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricsResultsHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sixrr.metrics.utils.MethodUtils.calculateUniqueSignature;
import static junit.framework.TestCase.fail;

public class MetricsResultsHolderTestImpl implements MetricsResultsHolder {
    private Map<Metric, Double> projectMetrics = new HashMap<>();
    private Map<Metric, Map<FileType, Double>> fileTypeMetrics = new HashMap<>();
    private Map<Metric, Map<Module, Double>> moduleMetrics = new HashMap<>();
    private Map<Metric, Map<PsiPackage, Double>> packageMetrics = new HashMap<>();
    private Map<Metric, Map<PsiClass, Double>> classMetrics = new HashMap<>();
    private Map<Metric, Map<PsiClass, Double>> interfaceMetrics = new HashMap<>();
    private Map<Metric, Map<PsiMethod, Double>> methodMetrics = new HashMap<>();
    private Map<Metric, Pair<Double, Double>> projectMetricsFractions = new HashMap<>();
    private Map<Metric, Map<FileType, Pair<Double, Double>>> fileTypeMetricsFractions = new HashMap<>();
    private Map<Metric, Map<Module, Pair<Double, Double>>> moduleMetricsFractions = new HashMap<>();
    private Map<Metric, Map<PsiPackage, Pair<Double, Double>>> packageMetricsFractions = new HashMap<>();
    private Map<Metric, Map<PsiClass, Pair<Double, Double>>> classMetricsFractions = new HashMap<>();
    private Map<Metric, Map<PsiClass, Pair<Double, Double>>> interfaceMetricsFractions = new HashMap<>();
    private Map<Metric, Map<PsiMethod, Pair<Double, Double>>> methodMetricsFractions = new HashMap<>();

    @Override
    public void postProjectMetric(Metric metric, double value) {

    }

    @Override
    public void postFileTypeMetric(Metric metric, FileType fileType, double value) {

    }

    @Override
    public void postModuleMetric(Metric metric, Module module, double value) {

    }

    @Override
    public void postPackageMetric(Metric metric, PsiPackage aPackage, double value) {

    }

    @Override
    public void postClassMetric(Metric metric, PsiClass aClass, double value) {
        classMetrics.computeIfAbsent(metric, ignored -> new HashMap<>()).put(aClass, value);
    }

    @Override
    public void postInterfaceMetric(Metric metric, PsiClass anInterface, double value) {

    }

    @Override
    public void postMethodMetric(Metric metric, PsiMethod method, double value) {
        methodMetrics.computeIfAbsent(metric, ignored -> new HashMap<>()).put(method, value);
    }

    @Override
    public void postProjectMetric(Metric metric, double numerator, double denominator) {

    }

    @Override
    public void postFileTypeMetric(Metric metric, FileType fileType, double numerator, double denominator) {

    }

    @Override
    public void postModuleMetric(Metric metric, Module module, double numerator, double denominator) {

    }

    @Override
    public void postPackageMetric(Metric metric, PsiPackage aPackage, double numerator, double denominator) {

    }

    @Override
    public void postClassMetric(Metric metric, PsiClass aClass, double numerator, double denominator) {

    }

    @Override
    public void postInterfaceMetric(Metric metric, PsiClass anInterface, double numerator, double denominator) {

    }

    @Override
    public void postMethodMetric(Metric metric, PsiMethod method, double numerator, double denominator) {
        methodMetricsFractions.computeIfAbsent(metric, ignored -> new HashMap<>()).
                put(method, new Pair<>(numerator, denominator));
    }

    private double getMethodMetric(Metric metric, PsiMethod method) {
        return methodMetrics.get(metric).get(method);
    }

    double getMethodMetric(Metric metric, String methodSignature) {
        return getMethodMetric(metric, findMethodBySignature(metric, methodSignature));
    }

    double getClassMetric(Metric metric, PsiClass aClass) {
        return classMetrics.get(metric).get(aClass);
    }

    double getClassMetric(Metric metric, String classQualifiedName) {
        return getClassMetric(metric, findClassByQualifiedName(metric, classQualifiedName));
    }

    private PsiClass findClassByQualifiedName(Metric metric, String classQualifiedName) {
        List<PsiClass> classes = classMetrics.get(metric).keySet().stream().
                filter(aClass -> Objects.equals(aClass.getQualifiedName(), classQualifiedName)).
                collect(Collectors.toList());

        if (classes.isEmpty()) {
            fail("Unknown method: " + classQualifiedName);
        }

        return classes.get(0);
    }

    private PsiMethod findMethodBySignature(Metric metric, String methodSignature) {
        List<PsiMethod> methods = methodMetrics.get(metric).keySet().stream().
                filter(method -> calculateUniqueSignature(method).equals(methodSignature)).
                collect(Collectors.toList());

        if (methods.isEmpty()) {
            fail("Unknown method: " + methodSignature);
        }

        return methods.get(0);
    }

    public Pair<Double, Double> getMethodMetricFraction(Metric metric, PsiMethod method) {
        return methodMetricsFractions.get(metric).get(method);
    }
}

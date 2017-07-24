/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.algorithm.sddrar;

import org.ml_methods_group.algorithm.sddrar.rules.Rule;
import org.ml_methods_group.algorithm.sddrar.rules.RuleExtractor;
import org.ml_methods_group.algorithm.sddrar.rules.RulePack;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.profile.MetricInstance;
import com.sixrr.metrics.profile.MetricsProfile;
import org.apache.commons.math3.linear.MatrixUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class SDDRARFacade {
    private static final double MIN_CONFIDENCE = 0.8;
    private static final double PERCENTAGE_OF_ERROR_THRESHOLD = 0.9;

    public static void trainAndPersistModel(MetricsRun metricsRun) {
        DataSet dataSet = extractDataSet(metricsRun);
        CorrelationFilter.filterByFeatureCorrelationRate(dataSet);
        List<String> interestingMetrics = dataSet.getFeatureNames();
        System.out.println(interestingMetrics);
        SDDRARioHandler.dumpMetrics(interestingMetrics);

        Set<Rule> rules = RuleExtractor.extractRules(dataSet, MIN_CONFIDENCE);
        RulePack pack = new RulePack(rules, dataSet.getFeatureNames());
        SDDRARioHandler.dumpRulePack(pack);
    }

    public static List<String> checkNewData(MetricsRun metricsRun) {
        DataSet dataSet = extractDataSet(metricsRun);
        List<String> interestingMetrics = dataSet.getFeatureNames();
        System.out.println(interestingMetrics);

        RulePack rulePack = SDDRARioHandler.loadRulePack();
        Set<Rule> rules = rulePack.fitRulesToDataSet(dataSet);

        List<Integer> faultyIndices = ErrorComputer.getFaultyEntities(dataSet, rules, PERCENTAGE_OF_ERROR_THRESHOLD);

        List<String> names = dataSet.getEntityNames();
        List<String> faultyNames = faultyIndices.stream().map(names::get).collect(Collectors.toList());

        Map<String, Set<Rule>> failed = ErrorComputer.getFailedRulesForEachEntity(dataSet, rules)
                .entrySet()
                .stream()
                .filter(entry -> faultyNames.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Map<Integer, Integer>> failedNumByClass = new HashMap<>();
        failed.forEach((key, value) -> {
            Map<Integer, Integer> res = new HashMap<>();
            value.forEach(rule -> rule.getBody().stream()
                    .filter(node -> node.getType() == Rule.Type.VALUE)
                    .forEach(node -> {
                        Integer prev = res.get(node.getValue());
                        if (prev == null) {
                            res.put(node.getValue(), 0);
                        } else {
                            res.put(node.getValue(), prev + 1);
                        }
                    }));
            failedNumByClass.put(key, res);
        });

        StringBuilder sb = new StringBuilder();

        failedNumByClass.forEach((key, value) -> {
            sb.append(key).append(":\n");
            value.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                    .forEachOrdered(entry -> {
                        sb.append(dataSet.getFeatureNames().get(entry.getKey()))
                                .append("->")
                                .append(entry.getValue())
                                .append('\n');
                    });
            sb.append("\n\n");
        });

        System.out.println(sb);

        return faultyNames;
    }

    // понять какие метрики на что влияют, какие лучшие
    // но вроде модем взять сразу все и хрен с ним
    // но нет, потому что на входе отсеиватся только те, у кого было |mean - stedev| < val
    // вопрос : как определить проблему по проваленным правилам?

    public static void selectInterestingMetrics(MetricsProfile profile) {

        List<String> interestingMetrics = SDDRARioHandler.loadMetrics();
        List<MetricInstance> metricInstances = profile.getMetricInstances();
        metricInstances.forEach(m -> m.setEnabled(interestingMetrics.contains(m.getMetric().getID())));
    }

    private static DataSet extractDataSet(MetricsRun metricsRun) {

        MetricsResult result = metricsRun.getResultsForCategory(MetricCategory.Class);
        List<String> entityNames = Arrays.asList(result.getMeasuredObjects());
        Metric[] metrics = result.getMetrics();
        List<String> featureNames = Arrays.stream(metrics).map(Metric::getID).collect(Collectors.toList());

        double[][] data = entityNames.stream()
                .map(entityName -> IntStream.range(0, featureNames.size())
                        .mapToObj(j -> result.getValueForMetric(metrics[j], entityName))
                        .mapToDouble(v -> v == null ? 0 : v)
                        .toArray())
                .toArray(double[][]::new);

        return new DataSet(MatrixUtils.createRealMatrix(data), entityNames, featureNames);
    }
}

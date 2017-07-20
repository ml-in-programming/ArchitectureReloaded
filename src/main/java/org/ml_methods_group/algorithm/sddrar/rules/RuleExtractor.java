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

package org.ml_methods_group.algorithm.sddrar.rules;

import org.ml_methods_group.algorithm.sddrar.DataSet;

import java.util.*;
import java.util.stream.Collectors;

public class RuleExtractor {

    //todo add confidence of rule to toString, e.g.: confidence=0.81
    public static Set<Rule> extractRules(DataSet dataSet, double minConfidence) {

        log("number of entities = %s", dataSet.getEntityNames().size());
        log("number of features = %s", dataSet.getFeatureNames().size());
        log("min confidence = %s", minConfidence);

        System.out.println(String.format("k = %s", 2));
        Set<Rule> candidates = findRulesOfSizeTwo(dataSet);
        System.out.println(String.format("candidates size  = %s", candidates.size()));
        Set<Rule> newRules = filterByConfidence(candidates, minConfidence, dataSet);
        Set<Rule> resultRules = newRules;
        System.out.println(String.format("result rules size = %s", resultRules.size()));

        int k = 3;
        int featuresNum = dataSet.getFeatureNames().size();
        while (!newRules.isEmpty() && k <= featuresNum) {
            System.out.println(String.format("k = %s", k));
            Map<Rule, Set<Rule>> childrenAndParents = genCandidates(newRules);
            candidates = childrenAndParents.keySet();
            System.out.println(String.format("candidates size  = %s", candidates.size()));

            newRules = filterByConfidence(candidates, minConfidence, dataSet);
            for (Rule interestingRule : newRules) {
                Set<Rule> usedAtProduction = childrenAndParents.get(interestingRule);
                if (usedAtProduction != null) {
                    resultRules.removeAll(usedAtProduction);
                }
            }

            resultRules.addAll(newRules);
            System.out.println(String.format("result rules size = %s", resultRules.size()));
            ++k;
        }

        return resultRules;
    }

    private static void printSorted(Set<Rule> rules) {
        rules.stream()
                .sorted(Comparator.comparingInt(r -> r.getBody().get(0).getValue()))
                .forEach(System.out::println);
    }

    private static void log(String pattern, Object ... objects) {
        System.out.println(String.format(pattern, objects));
    }

    private static void addToRulesMap(Map<Rule, Set<Rule>> rules, Rule newRule, Rule rule1, Rule rule2) {
        Set<Rule> producers = new HashSet<>();
        producers.add(rule1);
        producers.add(rule2);
        if (rules.containsKey(newRule)) {
            producers.addAll(rules.get(newRule));
        }
        rules.put(newRule, producers);
    }

    private static Map<Rule, Set<Rule>> genCandidates(Set<Rule> shorterRules) {
        Map<Rule, Set<Rule>> rules = new HashMap<>();
        for (Rule rule1 : shorterRules) {
            for (Rule rule2 : shorterRules) {
                if (!rule1.equals(rule2)) {
                    if (candidate1(rule1, rule2)) {
                        Rule newRule = create1(rule1, rule2);
                        addToRulesMap(rules, newRule, rule1, rule2);
                    } else if (candidate2(rule1, rule2)) {
                        Rule newRule = create2(rule1, rule2);
                        addToRulesMap(rules, newRule, rule1, rule2);
                    } else if (candidate3(rule1, rule2)) {
                        Rule newRule = create3(rule1, rule2);
                        addToRulesMap(rules, newRule, rule1, rule2);;
                    } else if (candidate4(rule1, rule2)) {
                        Rule newRule = create4(rule1, rule2);
                        addToRulesMap(rules, newRule, rule1, rule2);;
                    }
                }
            }
        }

        return rules;
    }

    private static boolean candidate1(Rule rule1, Rule rule2) {
        boolean differentAttributes = rule1.firstAttribute() != rule2.lastAttribute();
        return differentAttributes && rule1.cutLeft().equals(rule2.cutRight());
    }

    private static boolean candidate2(Rule rule1, Rule rule2) {
        boolean differentAttributes = rule1.lastAttribute() != rule2.firstAttribute();
        return differentAttributes && rule1.cutRight().equals(rule2.cutLeft());
    }

    private static boolean candidate3(Rule rule1, Rule rule2) {
        boolean differentAttributes = rule1.firstAttribute() != rule2.firstAttribute();
        return differentAttributes && rule1.cutLeft().equals(rule2.cutLeft().reversed());
    }

    private static boolean candidate4(Rule rule1, Rule rule2) {
        boolean differentAttributes = rule1.lastAttribute() != rule2.lastAttribute();
        return differentAttributes && rule1.cutRight().equals(rule2.cutRight().reversed());
    }

    private static Rule create1(Rule rule1, Rule rule2) {
        return rule1.appendRight(rule2.right());
    }

    private static Rule create2(Rule rule1, Rule rule2) {
        return rule1.appendLeft(rule2.left());
    }

    private static Rule create3(Rule rule1, Rule rule2) {
        return rule1.appendRight(rule2.reversed().right());
    }

    private static Rule create4(Rule rule1, Rule rule2) {
        return rule1.appendLeft(rule2.reversed().left());
    }

    private static Set<Rule> findRulesOfSizeTwo(DataSet dataSet) {
        Set<Rule> C2 = new HashSet<>();
        for(double[] entity : dataSet.getMatrix().getData()) {
            for (int i = 0; i < entity.length; i++) {
                for (int j = 0; j < i; j++) {
                    Rule rule = new Rule();
                    List<Rule.Node> nodes = new ArrayList<>();
                    nodes.add(new Rule.Node(i));
                    if (entity[i] < entity[j]) {
                        nodes.add(new Rule.Node(Rule.Type.LT));
                    } else if (entity[i] > entity[j]) {
                        nodes.add(new Rule.Node(Rule.Type.GT));
                    } else {
                        nodes.add(new Rule.Node(Rule.Type.EQ));
                    }
                    nodes.add(new Rule.Node(j));
                    rule.setBody(nodes);
                    C2.add(rule);
                }
            }
        }
        return C2;
    }

    private static Set<Rule> filterByConfidence(Set<Rule> rules, double minConfidence, DataSet dataSet) {
        return rules
                .stream()
                .filter(rule -> getConfidence(rule, dataSet) >= minConfidence)
                .collect(Collectors.toSet());
    }

    public static double getConfidence(Rule rule, DataSet dataSet) {
        int numberOfEntities = dataSet.getEntityNames().size();
        int numberOfSuccesses = 0;
        for (double[] entity : dataSet.getMatrix().getData()) {
            if (rule.check(entity)) {
                ++numberOfSuccesses;
            }
        }

        double confidence = (double) numberOfSuccesses / numberOfEntities;
        return confidence;
    }
}

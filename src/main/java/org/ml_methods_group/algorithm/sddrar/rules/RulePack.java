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

import java.io.Serializable;
import java.util.*;

/**
 * Created by boris on 23.05.17.
 */
public class RulePack implements Serializable {
    private static final long serialVersionUID = -7177265391350068971L;
    private Set<Rule> rules;
    private List<String> featureNames;

    public RulePack(Set<Rule> rules, List<String> featureNames) {
        this.rules = rules;
        this.featureNames = featureNames;
    }

    public Set<Rule> fitRulesToDataSet(DataSet dataSet) {

        Map<String, Integer> newIndices = new HashMap<>();
        for (int i = 0; i < dataSet.getFeatureNames().size(); i++) {
            newIndices.put(dataSet.getFeatureNames().get(i), i);
        }

        Set<Rule> resultRules = new HashSet<>();
        for (Rule rule : rules) {
            List<Rule.Node> body = new ArrayList<>();
            for (Rule.Node node : rule.getBody()) {
                if (node.getType() != Rule.Type.VALUE) {
                    body.add(new Rule.Node(node));
                } else {
                    Integer newIndex = newIndices.get(featureNames.get(node.getValue()));
                    body.add(new Rule.Node(newIndex));
                }
            }
            resultRules.add(Rule.fromBody(body));
        }

        return resultRules;
    }
}

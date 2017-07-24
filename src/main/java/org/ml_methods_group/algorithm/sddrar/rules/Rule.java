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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Rule implements Serializable {
    private static final long serialVersionUID = 5958237623192810723L;

    public enum Type {
        GT(">"),
        LT("<"),
        EQ("="),
        VALUE("");

        final String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class Node implements Serializable {
        private static final long serialVersionUID = 2483135937390934040L;
        private Type type;
        private final int value;

        Node(int value) {
            this.value = value;
            type = Type.VALUE;
        }

        Node(Type type) {
            this.value = 0;
            this.type = type;
        }

        Node(Node other) {
            value = other.value;
            type = other.type;
        }

        public static Node valueOf(String token) {
            switch (token){
                case ">":
                    return new Node(Type.GT);
                case "<":
                    return new Node(Type.LT);
                case "=":
                    return new Node(Type.EQ);
                default:
                    return new Node(Integer.parseInt(token));
            }
        }

        public Type getType() {
            return type;
        }

        public Node inverseType() {
            if (type == Type.LT) {
                type = Type.GT;
            } else if (type == Type.GT) {
                type = Type.LT;
            }
            return this;
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Node node = (Node) o;

            return value == node.value && type == node.type;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + value;
            return result;
        }

        @Override
        public String toString() {
            return type == Type.VALUE ? String.valueOf(value) : type.toString();
        }
    }

    private List<Node> body = new ArrayList<>();
    private List<Node> reversedBody = new ArrayList<>();


    public static Rule valueOf(String string) {
        final String[] tokens = string.split(" ");
        final List<Node> ruleBody = new ArrayList<>();
        for (final String token : tokens) {
            ruleBody.add(Node.valueOf(token));
        }
        Rule rule = new Rule();
        rule.setBody(ruleBody);
        return rule;
    }
    public static Rule fromBody(List<Node> body) {
        Rule rule = new Rule();
        rule.setBody(body);
        return rule;
    }

    public List<Node> getBody() {
        return body;
    }

    public void setBody(List<Node> body) {
        this.body = body;
        reversedBody = cloneNodes(body);
        reversedBody = reversedBody.stream().map(Node::inverseType).collect(Collectors.toList());
        Collections.reverse(reversedBody);
    }


    public Rule cutLeft() {
        final Rule newRule = new Rule();
        final List<Node> newNodes = new ArrayList<>();
        for (int i = 2; i < body.size(); i++) {
            newNodes.add(body.get(i));
        }
        newRule.setBody(newNodes);
        return newRule;
    }

    public Rule cutRight() {
        final Rule newRule = new Rule();
        final List<Node> newNodes = new ArrayList<>();
        for (int i = 0; i < body.size() - 2; i++) {
            newNodes.add(body.get(i));
        }
        newRule.setBody(newNodes);
        return newRule;
    }

    public Rule reversed() {
        final Rule newRule = clone();
        newRule.body.stream().map(Node::inverseType).collect(Collectors.toList());
        Collections.reverse(newRule.body);
        return newRule;
    }

    public Rule appendRight(Rule rule) {
        final Rule newRule = new Rule();
        final List<Node> newNodes = new ArrayList<>(body.size() + rule.getBody().size());
        newNodes.addAll(body);
        newNodes.addAll(rule.getBody());
        newRule.setBody(newNodes);
        return newRule;
    }

    public Rule appendLeft(Rule rule) {
        final Rule newRule = new Rule();
        final List<Node> newNodes = new ArrayList<>(body.size() + rule.getBody().size());
        newNodes.addAll(rule.getBody());
        newNodes.addAll(body);
        newRule.setBody(newNodes);
        return newRule;
    }

    public Rule left() {
        final Rule newRule = new Rule();
        final List<Node> newNodes = new ArrayList<>();
        newNodes.add(body.get(0));
        newNodes.add(body.get(1));
        newRule.setBody(newNodes);
        return newRule;
    }

    public Rule right() {
        final Rule newRule = new Rule();
        final List<Node> newNodes = new ArrayList<>();
        newNodes.add(body.get(body.size() - 2));
        newNodes.add(body.get(body.size() - 1));
        newRule.setBody(newNodes);
        return newRule;
    }

    public Rule clone() {
        final Rule clone = new Rule();
        clone.setBody(cloneNodes(body));
        return clone;
    }

    private List<Node> cloneNodes(List<Node> toClone) {
        return toClone.stream().map(Node::new).collect(Collectors.toList());
    }

    public int firstAttribute() {
        return body.get(0).getValue();
    }

    public int lastAttribute() {
        return body.get(body.size() - 1).getValue();
    }

    public boolean check(double[] entity) {
        boolean result = true;

        for (int i = 1; i < body.size() - 1; i += 2) {
            if (body.get(i).type == Type.GT) {
                result &= entity[body.get(i - 1).getValue()] > entity[body.get(i + 1).getValue()];
            } else if (body.get(i).type == Type.LT) {
                result &= entity[body.get(i - 1).getValue()] < entity[body.get(i + 1).getValue()];
            } else {
                result &= entity[body.get(i - 1).getValue()] == entity[body.get(i + 1).getValue()];
            }
        }

        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Rule rule = (Rule) o;

        return body != null ? body.equals(rule.body) || reversedBody.equals(rule.body) : rule.body == null;
    }

    @Override
    public int hashCode() {
        return body != null ? body.hashCode() + reversedBody.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "body=" + body +
                "}\n";
    }

    public String toVerboseString(DataSet dataSet) {

        final StringBuilder sb = new StringBuilder();
        final List<String> featureNames = dataSet.getFeatureNames();

        for (Node node : body) {
            if (node.getType() == Type.VALUE) {
                sb.append(featureNames.get(node.getValue()));
            } else {
                sb.append(" ").append(node.getType()).append(" ");
            }
        }

        return sb.toString();
    }
}

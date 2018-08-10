package com.sixrr.stockmetrics.methodCalculators;

public class NumCalledMethodsCalculator extends AbstractNumCalledMethodsCalculator {
    public NumCalledMethodsCalculator() {
        super((calledMethod, currentMethod) -> true);
    }
}

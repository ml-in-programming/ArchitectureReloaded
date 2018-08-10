package com.sixrr.stockmetrics.methodCalculators;

public class NumMethodsThatCallCalculator extends AbstractNumMethodsThatCallCalculator {
    public NumMethodsThatCallCalculator() {
        super((callingMethod, currentMethod) -> true);
    }
}

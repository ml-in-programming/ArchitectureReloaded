package org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers;

public class RefactoringsFileParsers {
    private final static ParserForJMoveDataSet PARSER_FOR_JMOVE_DATA_SET = new ParserForJMoveDataSet();

    public static RefactoringsFileParser getParserForJMoveDataSet() {
        return PARSER_FOR_JMOVE_DATA_SET;
    }
}

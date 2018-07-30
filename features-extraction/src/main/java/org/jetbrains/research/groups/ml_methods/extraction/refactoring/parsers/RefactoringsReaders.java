package org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers;

public class RefactoringsReaders {
    private final static JMoveReader JMOVE_READER = new JMoveReader();
    private final static JBReader JB_READER = new JBReader();

    public static RefactoringsReader getJMoveReader() {
        return JMOVE_READER;
    }

    public static RefactoringsReader getJBReader() {
        return JB_READER;
    }
}

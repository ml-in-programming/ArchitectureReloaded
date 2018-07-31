package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

public class RefactoringsWriters {
    private final static JMoveWriter JMOVE_WRITER = new JMoveWriter();
    private final static JBWriter JB_WRITER = new JBWriter();

    public static RefactoringsWriter getJMoveWriter() {
        return JMOVE_WRITER;
    }

    public static RefactoringsWriter getJBWriter() {
        return JB_WRITER;
    }
}

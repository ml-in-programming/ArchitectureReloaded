package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RefactoringsWriters {
    private final static JMoveWriter JMOVE_WRITER = new JMoveWriter();
    private final static JBWriter JB_WRITER = new JBWriter();
    private final static List<RefactoringsWriter> AVAILABLE_WRITERS = Arrays.asList(JMOVE_WRITER, JB_WRITER);

    public static RefactoringsWriter getJMoveWriter() {
        return JMOVE_WRITER;
    }

    public static RefactoringsWriter getJBWriter() {
        return JB_WRITER;
    }

    public static Optional<RefactoringsWriter> getWriterByName(String name) {
        return AVAILABLE_WRITERS.stream()
                .filter(refactoringsReader -> refactoringsReader.getName().equals(name))
                .findAny();
    }
}

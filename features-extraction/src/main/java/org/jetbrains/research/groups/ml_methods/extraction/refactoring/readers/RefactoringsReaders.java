package org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RefactoringsReaders {
    private final static JMoveReader JMOVE_READER = new JMoveReader();
    private final static JBReader JB_READER = new JBReader();
    private final static List<RefactoringsReader> AVAILABLE_READERS = Arrays.asList(JMOVE_READER, JB_READER);

    public static RefactoringsReader getJMoveReader() {
        return JMOVE_READER;
    }

    public static RefactoringsReader getJBReader() {
        return JB_READER;
    }

    public static Optional<RefactoringsReader> getReaderByName(String name) {
        return AVAILABLE_READERS.stream()
                .filter(refactoringsReader -> refactoringsReader.getName().equals(name))
                .findAny();
    }
}

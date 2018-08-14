package org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JBRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JBReaderTest {
    private final static Path REFACTORINGS_PATH = Paths.get("./src/test/resources/JBRefactorings");

    @Test
    public void readFromFile() throws IOException {
        checkReadRefactorings(RefactoringsReaders.getJBReader().read(REFACTORINGS_PATH));
    }

    @Test
    public void readFromInputStream() throws IOException {
        checkReadRefactorings(RefactoringsReaders.getJBReader().read(new FileInputStream(REFACTORINGS_PATH.toFile())));
    }

    private void checkReadRefactorings(List<RefactoringTextRepresentation> refactorings) {
        List<RefactoringTextRepresentation> expectedRefactorings = Arrays.asList(
                new JBRefactoringTextRepresentation(
                        "org.jhotdraw.samples.svg.gui.ViewToolBar", "setEditor",
                        Collections.singletonList("int"), "org.jhotdraw.samples.svg.SVGDrawingPanel"
                ),
                new JBRefactoringTextRepresentation(
                        "org.jhotdraw.samples.svg.io.SVGInputFormat", "readTransformAttribute",
                        Collections.emptyList(), "org.jhotdraw.draw.AttributeKeys"
                ),
                new JBRefactoringTextRepresentation(
                        "net.n3.nanoxml.XMLElement", "print",
                        Arrays.asList("java.lang.Integer", "java.util.List<java.lang.Integer>"), "org.jhotdraw.xml.NanoXMLDOMOutput"
                )
        );
        assertEquals(expectedRefactorings, refactorings);
    }
}
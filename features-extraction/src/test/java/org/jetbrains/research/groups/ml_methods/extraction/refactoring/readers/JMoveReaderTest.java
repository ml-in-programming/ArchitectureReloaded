package org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JMoveRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JMoveReaderTest {
    private final static Path REFACTORINGS_PATH = Paths.get("./src/test/resources/refactorings");

    @Test
    public void read() throws IOException {
        List<RefactoringTextRepresentation> refactorings = RefactoringsReaders.getJMoveReader().read(REFACTORINGS_PATH);
        List<RefactoringTextRepresentation> expectedRefactorings = Arrays.asList(
                new JMoveRefactoringTextRepresentation(
                        "org.jhotdraw.samples.svg.gui.ViewToolBar", "setEditor",
                        Collections.emptyList(), "org.jhotdraw.samples.svg.SVGDrawingPanel"
                ),
                new JMoveRefactoringTextRepresentation(
                        "net.n3.nanoxml.XMLElement", "print",
                        Collections.emptyList(), "org.jhotdraw.xml.NanoXMLDOMOutput"
                ),
                new JMoveRefactoringTextRepresentation(
                        "net.n3.nanoxml.XMLElement", "withParams",
                        Arrays.asList("Movie", "Rental"), "org.jhotdraw.xml.NanoXMLDOMOutput"
                ),
                new JMoveRefactoringTextRepresentation(
                        "net.n3.nanoxml.XMLElement", "withOneParam",
                        Collections.singletonList("Price"), "org.jhotdraw.xml.NanoXMLDOMOutput"
                ),
                new JMoveRefactoringTextRepresentation(
                        "org.jhotdraw.samples.svg.io.SVGInputFormat", "readTransformAttribute",
                        Arrays.asList("IXMLElement", "HashMap<AttributeKey, Object>"), "org.jhotdraw.draw.AttributeKeys"
                )
        );
        assertEquals(expectedRefactorings, refactorings);
    }
}
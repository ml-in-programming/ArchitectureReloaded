package org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JMoveRefactoringTextRepresentation;
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

public class JMoveReaderTest {
    private final static Path REFACTORINGS_PATH = Paths.get("./src/test/resources/JMoveRefactorings");

    @Test
    public void readFromFile() throws IOException {
        checkReadRefactorings(RefactoringsReaders.getJMoveReader().read(REFACTORINGS_PATH));

    }

    @Test
    public void readFromInputStream() throws IOException {
        checkReadRefactorings(RefactoringsReaders.getJMoveReader().read(new FileInputStream(REFACTORINGS_PATH.toFile())));
    }

    private void checkReadRefactorings(List<RefactoringTextRepresentation> refactorings) {
        List<RefactoringTextRepresentation> expectedRefactorings = Arrays.asList(
                new JMoveRefactoringTextRepresentation(
                        "org.jhotdraw.samples.svg.gui.ViewToolBar", "setEditor",
                        Collections.singletonList("void"), "org.jhotdraw.samples.svg.SVGDrawingPanel"
                ),
                new JMoveRefactoringTextRepresentation(
                        "net.n3.nanoxml.XMLElement", "print",
                        Collections.singletonList("void"), "org.jhotdraw.xml.NanoXMLDOMOutput"
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
                ),
                new JMoveRefactoringTextRepresentation(
                        "org.argouml.uml.ui.SaveGraphicsManager", "getDefaultSuffix",
                        Collections.singletonList("void"), "org.argouml.uml.ui.ActionSaveAllGraphics"
                )
        );
        assertEquals(expectedRefactorings, refactorings);
    }
}
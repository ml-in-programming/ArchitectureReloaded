package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JMoveRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JMoveWriterTest {
    private final static Path REFACTORINGS_PATH_TO_COMPARE = Paths.get("./src/test/resources/JMoveRefactorings");
    private final static List<RefactoringTextRepresentation> REFACTORINGS_TO_WRITE = Arrays.asList(
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
            ),
            new JMoveRefactoringTextRepresentation(
                    "org.argouml.uml.ui.SaveGraphicsManager", "getDefaultSuffix",
                    Collections.singletonList("void"), "org.argouml.uml.ui.ActionSaveAllGraphics"
            )
    );

    @Test
    public void writeToFile() throws IOException {
        Path fileToWrite = Files.createTempFile(null, null);
        fileToWrite.toFile().deleteOnExit();
        RefactoringsWriters.getJMoveWriter().writeRefactoringsInTextForm(REFACTORINGS_TO_WRITE, fileToWrite);
        checkWrittenRefactorings(fileToWrite.toFile());
    }

    @Test
    public void writeToOutputStream() throws IOException {
        Path fileToWrite = Files.createTempFile(null, null);
        fileToWrite.toFile().deleteOnExit();
        RefactoringsWriters.getJMoveWriter().writeRefactoringsInTextForm(REFACTORINGS_TO_WRITE,
                new FileOutputStream(fileToWrite.toFile()));
        checkWrittenRefactorings(fileToWrite.toFile());
    }

    private void checkWrittenRefactorings(File writtenFile) throws IOException {
        assertEquals(Files.readAllLines(writtenFile.toPath()), Files.readAllLines(REFACTORINGS_PATH_TO_COMPARE));
    }
}
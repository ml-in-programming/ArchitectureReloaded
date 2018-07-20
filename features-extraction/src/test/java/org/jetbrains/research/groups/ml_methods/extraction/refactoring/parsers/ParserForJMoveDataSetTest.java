package org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.TextFormRefactoring;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class ParserForJMoveDataSetTest {
    private final static Path REFACTORINGS_PATH = Paths.get("./src/test/resources/refactorings");

    @Test
    public void parse() throws IOException {
        Set<TextFormRefactoring> refactorings = (new ParserForJMoveDataSet()).parse(REFACTORINGS_PATH);
        Set<TextFormRefactoring> expectedRefactorings = new HashSet<>(Arrays.asList(
                new TextFormRefactoring(
                        "org.jhotdraw.samples.svg.gui.ViewToolBar", "setEditor",
                        Collections.emptyList(), "org.jhotdraw.samples.svg.SVGDrawingPanel"
                ),
                new TextFormRefactoring(
                        "net.n3.nanoxml.XMLElement", "withParams",
                        Arrays.asList("Movie", "Rental"), "org.jhotdraw.xml.NanoXMLDOMOutput"
                ),
                new TextFormRefactoring(
                        "net.n3.nanoxml.XMLElement", "withOneParam",
                        Collections.singletonList("Price"), "org.jhotdraw.xml.NanoXMLDOMOutput"
                ),
                new TextFormRefactoring(
                        "net.n3.nanoxml.XMLElement", "print",
                        Collections.emptyList(), "org.jhotdraw.xml.NanoXMLDOMOutput"
                )
        ));
        assertEquals(expectedRefactorings, refactorings);
    }
}
package org.jetbrains.research.groups.ml_methods.refactoring.validation;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class InputSessionValidatorTest {
    private InputSessionValidator validator;
    private SimpleSessionValidationResult result;
    private final String LOGS_PATH = "src/test/resources/logs/";

    @Before
    public void setup() {
        result = new SimpleSessionValidationResult();
        validator = new InputSessionValidator(result);
    }

    @Test
    public void testCorrect1() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "correctTest1.txt");
        validator.validate(entries);
        assertEquals(1, result.validLines().size());
        assertEquals(0, result.errorLines().size());
    }

    @Test
    public void testCorrect2() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "correctTest2.txt");
        validator.validate(entries);
        assertEquals(1, result.validLines().size());
        assertEquals(0, result.errorLines().size());
    }

    @Test
    public void testCorrect3() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "correctTest3.txt");
        validator.validate(entries);
        assertEquals(1, result.validLines().size());
        assertEquals(0, result.errorLines().size());
    }

    @Test
    public void testIncorrectTimestamp() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "incorrectTest1.txt");
        validator.validate(entries);
        assertEquals(0, result.validLines().size());
        assertEquals(1, result.errorLines().size());
    }

    @Test
    public void testIncorrectUsedId() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "incorrectTest2.txt");
        validator.validate(entries);
        assertEquals(0, result.validLines().size());
        assertEquals(1, result.errorLines().size());
    }

    @Test
    public void testIncorrectSessionId() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "incorrectTest3.txt");
        validator.validate(entries);
        assertEquals(0, result.validLines().size());
        assertEquals(1, result.errorLines().size());
    }

    @Test
    public void testBothEmpty() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "incorrectTest4.txt");
        validator.validate(entries);
        assertEquals(0, result.validLines().size());
        assertEquals(1, result.errorLines().size());
    }

    @Test
    public void testRejectedMissing() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "incorrectTest5.txt");
        validator.validate(entries);
        assertEquals(0, result.validLines().size());
        assertEquals(1, result.errorLines().size());
    }

    @Test
    public void testIncorrectJsonInPayload() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "incorrectTest6.txt");
        validator.validate(entries);
        assertEquals(0, result.validLines().size());
        assertEquals(1, result.errorLines().size());
    }

    @Test
    public void testEmptyPayload() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "incorrectTest7.txt");
        validator.validate(entries);
        assertEquals(0, result.validLines().size());
        assertEquals(1, result.errorLines().size());
    }


    private List<String> loadLogs(String path) throws IOException {
        return Files.lines(Paths.get(path))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
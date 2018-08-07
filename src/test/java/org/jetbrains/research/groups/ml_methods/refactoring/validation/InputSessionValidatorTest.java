package org.jetbrains.research.groups.ml_methods.refactoring.validation;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
    }

    @Test
    public void testCorrect2() throws IOException {
        List<String> entries = loadLogs(LOGS_PATH + "correctTest2.txt");
        validator.validate(entries);
        assertEquals(1, result.validLines().size());
    }


    private List<String> loadLogs(String path) throws IOException {
        return Files.lines(Paths.get(path))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
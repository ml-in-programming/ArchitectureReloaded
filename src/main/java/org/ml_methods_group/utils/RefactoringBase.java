/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.utils;

import com.intellij.openapi.project.Project;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RefactoringBase {
    private static final Map<String, RefactoringBase> INSTANCES = new HashMap<>();

    public enum Status {UNKNOWN, GOOD, BAD, VERY_GOOD, VERY_BAD}

    private final Map<String, Status> refactoringBase;

    private RefactoringBase(Project project) {
        final File file = Paths.get(project.getBasePath() + "", "refactorings_base.txt").toFile();
        refactoringBase = new HashMap<>();
        try (FileInputStream fileStream = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream))) {
            while (true) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                final String[] data = line.split(" ");
                if (data.length != 2) {
                    System.err.println("Skipped: " + line);
                    continue;
                }
                refactoringBase.put(data[0], Status.valueOf(data[1]));
            }
        } catch (Exception ignored) {
        }
    }

    public Status getStatusFor(String member, String target) {
        return refactoringBase.getOrDefault(member + "->" + target, Status.UNKNOWN);
    }

    public void setStatus(String member, String target, Status status) {
        refactoringBase.put(member + "->" + target, status);
    }

    public void save(Project project) {
        final File file = Paths.get(project.getBasePath() + "", "refactorings_base.txt").toFile();
        try (PrintWriter writer = new PrintWriter(file)) {
            refactoringBase.entrySet().stream()
                    .map(e -> e.getKey() + " " + e.getValue())
                    .forEachOrdered(writer::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static RefactoringBase getInstance(Project project) {
        return INSTANCES.computeIfAbsent(project.getName(), x -> new RefactoringBase(project));
    }
}

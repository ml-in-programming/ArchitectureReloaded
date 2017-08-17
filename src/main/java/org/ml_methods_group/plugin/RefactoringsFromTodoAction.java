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

package org.ml_methods_group.plugin;

import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RefactoringsFromTodoAction extends BaseAnalysisAction {

    protected RefactoringsFromTodoAction() {
        super("Refactorings from TODO's", "Refactorings");
    }

    @Override
    protected void analyze(@NotNull Project project, @NotNull AnalysisScope scope) {
        new Task.Backgroundable(project, "Write Refactorings from TODO's...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                dumpRefactoringsFromTodos(scope);
            }
        }.queue();
    }

    private void dumpRefactoringsFromTodos(@NotNull AnalysisScope scope) {
        final Map<String, String> refactorings = new HashMap<>();
        scope.accept(new JavaRecursiveElementWalkingVisitor() {
            @Override
            public void visitComment(PsiComment comment) {
                super.visitComment(comment);
                final PsiElement parent = comment.getParent();
                if (!(parent instanceof PsiMethod) || !comment.getText().toLowerCase().contains(" todo move to ")) {
                    return;
                }
                final PsiMethod method = (PsiMethod) parent;
                final PsiClass aClass = method.getContainingClass();
                if (aClass == null) {
                    return;
                }
                final String key = String.format("%s.%s(%s)", aClass.getName(), method.getName(),
                        String.join(", ", Arrays.stream(method.getParameterList().getParameters())
                                .map(p -> p.getType().getPresentableText())
                                .collect(Collectors.toList())
                        ));
                final String text = comment.getText();
                final String[] tokens = text.split("[ ]");
                final String value = tokens[tokens.length - 1];
                refactorings.put(key, value);
            }
        });
        dumpRefactorings(refactorings);
    }

    private void dumpRefactorings(Map<String, String> refactorings) {
            final File f = new File(System.getProperty("user.home") + "/Refactorings/table.txt");
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try (final FileWriter w = new FileWriter(f, false)) {
                final int maxMemberLength = Math.max(memberColumnName.length(), refactorings.keySet().stream()
                        .mapToInt(String::length)
                        .max()
                        .orElse(0)
                );
                final int maxClassLength = Math.max(classColumnName.length(), refactorings.values().stream()
                        .mapToInt(String::length)
                        .max()
                        .orElse(0)
                );
                writeHeader(w, maxMemberLength, maxClassLength);
                w.append(System.lineSeparator());
                w.append(String.join(System.lineSeparator(), refactorings.entrySet().stream()
                        .sorted(Comparator.comparing(Map.Entry::getValue))
                        .map(e -> String.format("| %s%s | %s%s |",
                                getMemberName(e.getKey()),
                                String.join("", getSpaces(maxMemberLength - e.getKey().length())),
                                getClassName(e.getValue()),
                                String.join("", getSpaces(maxClassLength - e.getValue().length()))
                                ))
                        .collect(Collectors.toList())));

            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private static final String memberColumnName = "Member";
    private static final String classColumnName = "Move to";
    private void writeHeader(Writer w, int maxMemberLength, int maxClassLength) throws IOException {
        w.append(String.format("| %s%s | %s%s |",
                memberColumnName,
                getSpaces(maxMemberLength - memberColumnName.length()),
                classColumnName,
                getSpaces(maxClassLength - classColumnName.length())
        ));
        w.append(System.lineSeparator());
        w.append(String.format("|%s|%s|", repeatChar('-', maxMemberLength + 2), repeatChar('-', maxClassLength + 2)));
    }

    private String getSpaces(final int n) {
        return repeatChar(' ', n);
    }

    private String repeatChar(char c, int n) {
        char[] chars = new char[n];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    private String getMemberName(String fullName) {
        final String[] names = fullName.split("[.]");
        final int n = names.length;
        return String.format("%s.%s", names[n - 2], names[n - 1]);
    }

    private String getClassName(String fullName) {
        final String[] names = fullName.split("[.]");
        final int n = names.length;
        return names[n - 1];
    }
}

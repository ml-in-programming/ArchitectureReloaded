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

import org.ml_methods_group.algorithm.LabelPropagationAdapter.Refactoring;
import org.ooxo.openapi.Label;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LabeledRefactorings {
    private final static String REFACTORINGS_FILE_NAME = "labeled_refactorings.data";
    private static File refactorings = null;
    private static Set<LabeledRefactoring> labeledRefactorings = null;
    private static Set<LabeledRefactoring> labeledRefactoringsTmp = null;

    static {
        refactorings = new File(REFACTORINGS_FILE_NAME);
        try {
            refactorings.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<LabeledRefactoring> getLabeledRefactorings() {
        loadIfNeeded();
        return labeledRefactorings;
    }

    public static void add(final Refactoring r, final Label l) {
    }

    public static void dump(Set<LabeledRefactoring> rs) {
        try {
            final OutputStream os = new FileOutputStream(refactorings);
            final ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(rs);
            oos.close();
//            labeledRefactorings = new HashSet<>(rs);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean needToLoad() {
        return labeledRefactorings == null;
    }

    private static void loadIfNeeded() {
        if (needToLoad()) {
            load();
        }
    }

    private static void load() {
        labeledRefactorings = new HashSet<>();
        try {
            final InputStream is = new FileInputStream(refactorings);
            final ObjectInputStream ois = new ObjectInputStream(is);
            labeledRefactorings = (Set<LabeledRefactoring>) ois.readObject();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class LabeledRefactoring implements Serializable{
        private static final long serialVersionUID = 3800749245696491548L;
        public final Refactoring refactoring;
        public final Label label;

        public LabeledRefactoring(Refactoring refactoring, Label label) {
            this.refactoring = refactoring;
            this.label = label;
        }

        @Override
        public int hashCode() {
            return 53 * refactoring.hashCode() + label.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LabeledRefactoring) {
                final LabeledRefactoring other = ((LabeledRefactoring) obj);
                return Objects.equals(refactoring, other.refactoring) && Objects.equals(label, other.label);
            }
            return false;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}

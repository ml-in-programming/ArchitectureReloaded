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

import org.jetbrains.annotations.Contract;
import org.ooxo.openapi.Label;

public class LabelUtils {
    private final static int GOOD_LABEL_ID = 1;
    private final static int BAD_LABEL_ID = 2;
    private final static Label goodLabel = new Label(GOOD_LABEL_ID);
    private final static Label badLabel = new Label(BAD_LABEL_ID);

    public static Label getGoodLabel() {
        return goodLabel;
    }

    public static Label getBadLabel() {
        return badLabel;
    }

    public static Label getEmptyLabel() {
        return Label.NO_LABEL;
    }

    @Contract(pure = true, value = "null -> false")
    public static boolean isGood(final Label l) {
        return l != null && l.getId() == GOOD_LABEL_ID;
    }

    @Contract(pure = true, value = "null -> false")
    public static boolean isBad(final Label l) {
        return l != null && l.getId() == BAD_LABEL_ID;
    }
}

/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.algorithm.attributes;

import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.CodeEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.utils.PSIUtil;

public class MethodAttributes extends ElementAttributes {
    private final @NotNull MethodEntity methodEntity;

    public MethodAttributes(
        final @NotNull MethodEntity methodEntity,
        final @NotNull double[] features
    ) {
        super(features);
        this.methodEntity = methodEntity;
    }

    @Override
    public @NotNull CodeEntity getOriginalEntity() {
        return methodEntity;
    }

    public @NotNull MethodEntity getOriginalMethod() {
        return methodEntity;
    }
}

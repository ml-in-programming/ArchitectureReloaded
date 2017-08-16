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

package org.ml_methods_group.optimization;

import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.PropertiesStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static org.ml_methods_group.algorithm.entity.PropertiesStrategy.*;

public class Optimizer {
    private final Random random = new Random(239566);
    private final List<BiFunction<Entity, Entity, Double>> calculators =
            Arrays.asList(SIMPLE_AVERAGE_CALCULATOR, SIMPLE_MAX_CALCULATOR, SIMPLE_MIN_CALCULATOR, EXP_CALCULATOR);

//    private PropertiesStrategy mutate(PropertiesStrategy strategy) {
//        final int fieldToMutate = random.nextInt(20);
//        switch (fieldToMutate) {
//            case 0 :
//        }
//    }


}

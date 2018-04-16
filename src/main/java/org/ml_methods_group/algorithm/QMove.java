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

package org.ml_methods_group.algorithm;

import org.ml_methods_group.algorithm.entity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class QOMMR extends Algorithm {
    private List<MethodEntity> methodEntities = new ArrayList<>();
    private final List<QMoveClassEntity> classes = new ArrayList<>();
    private final Map<String, ClassEntity> classesByName = new HashMap<>();
    QOMMR() {
        super("Quality-orientated Move Method Refactoring", false);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(
            ExecutionContext context, boolean enableFieldRefactorings) throws Exception {

        final EntitySearchResult searchResult = context.getEntities();
        methodEntities.clear();
        classes.clear();
        Stream.of(searchResult.getMethods())
                .flatMap(List::stream)
                .filter(Entity::isMovable)
                .forEach(methodEntities::add);

        searchResult.getClasses()
                .stream()
                .map(ClassEntity::copy) // create local copies
                .peek(entity -> classesByName.put(entity.getName(), entity))
                .forEach(classes::add);
        for(MethodEntity methodEntity : methodEntities){
            //getTargets


        }
        return null;
    }


    //number of classes -- projectMetrics.NumClassesProjectMetrics.java
    //PolymorphismFactorProjectMetric
    //NumInterfacesImplementedMetrics ?? super class
    //NumPrivateAttributes / NumAttributes
    //Number of classes where this class is used.
    /*
    Computes the relatedness among methods of the class based upon the
     parameter list of the methods. The metrics is computed using the
     summation of the intersection of parameters of a method with the
     maximum independent set of all parameter types in the class.
     */
    //Number of declarations with user classes
    //
    /*
   MethodInheritanceFactorProjectMetrics
     */
    //number of polymorphic methods ??
    //number of public methods
    //number of methods
}

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

package org.ml_methods_group.algorithm.entity;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.psi.*;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class PropertiesStrategy {

    public static final BiFunction<Entity, Entity, Double> SIMPLE_MIN_CALCULATOR = (first, second) -> {
        final RelevantProperties firstProperties = first.getRelevantProperties();
        final RelevantProperties secondProperties = second.getRelevantProperties();
        final int rpIntersect = firstProperties.sizeOfIntersection(secondProperties, Math::min);
        if (rpIntersect == 0) {
            return Double.POSITIVE_INFINITY;
        }
        final double ans = (1 - rpIntersect /
                (1.0 * firstProperties.sizeOfUnion(secondProperties)));
        return Math.sqrt(ans);
    };

    public static final BiFunction<Entity, Entity, Double> SIMPLE_MAX_CALCULATOR = (first, second) -> {
        final RelevantProperties firstProperties = first.getRelevantProperties();
        final RelevantProperties secondProperties = second.getRelevantProperties();
        final int rpIntersect = firstProperties.sizeOfIntersection(secondProperties, Math::max);
        if (rpIntersect == 0) {
            return Double.POSITIVE_INFINITY;
        }
        final double ans = (1 - rpIntersect /
                (1.0 * firstProperties.sizeOfUnion(secondProperties)));
        return Math.sqrt(ans);
    };

    public static final BiFunction<Entity, Entity, Double> SIMPLE_AVERAGE_CALCULATOR = (first, second) -> {
        final RelevantProperties firstProperties = first.getRelevantProperties();
        final RelevantProperties secondProperties = second.getRelevantProperties();
        final int rpIntersect = firstProperties.sizeOfIntersection(secondProperties, (x, y) -> (x + y) / 2);
        if (rpIntersect == 0) {
            return Double.POSITIVE_INFINITY;
        }
        final double ans = (1 - rpIntersect /
                (1.0 * firstProperties.sizeOfUnion(secondProperties)));
        return Math.sqrt(ans);
    };

    public static final BiFunction<Entity, Entity, Double> EXP_CALCULATOR = (first, second) -> {
        final RelevantProperties firstProperties = first.getRelevantProperties();
        final RelevantProperties secondProperties = second.getRelevantProperties();
        final int rpIntersect = firstProperties.sizeOfIntersection(secondProperties, (x, y) -> (x + y) / 2);
        if (rpIntersect == 0) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.exp(-rpIntersect);
    };

    private static final List<BiFunction<Entity, Entity, Double>> DEFAULT_CALCULATORS =
            Arrays.asList(SIMPLE_MIN_CALCULATOR, SIMPLE_MAX_CALCULATOR, SIMPLE_AVERAGE_CALCULATOR, EXP_CALCULATOR);

//    HAC
//    public static final PropertiesStrategy DEFAULT_STRATEGY = new PropertiesStrategy(1,
//            1, 4, 0, 12, 5,
//            8,3, 44, 0, 5,
//            45,28, 0, 39, 44,
//            4,0, 6, SIMPLE_MIN_CALCULATOR);

//    ARI
//    public static final PropertiesStrategy DEFAULT_STRATEGY = new PropertiesStrategy(18,
//            7, 46, 0, 40, 46,
//            49,0, 49, 29, 37,
//            18,37, 27, 42, 14,
//            4,0, 32, EXP_CALCULATOR);


    //    ARI (21.08.2017)
    public static final PropertiesStrategy DEFAULT_STRATEGY = new PropertiesStrategy(0,
            42, 36, 4, 1, 1,
            39,0, 7, 35, 33,
            9,25, 2, 5, 6,
            24,38, 4, SIMPLE_MIN_CALCULATOR);



    public final int methodCallPublicMethod;
    public final int methodCallStaticMethod;
    public final int methodCallPrivateMethod;
    public final int methodCalledByMethod;
    public final int methodUsePublicField;
    public final int methodUseStaticField;
    public final int methodUsePrivateField;
    public final int methodUseClassMember;
    public final int methodContainedByClass;
    public final int staticMethodContainedByClass;

    public final int fieldUsedByMethod;
    public final int fieldContainedByClass;
    public final int staticFieldUsedByMethod;
    public final int staticFieldContainedByClass;

    public final int classContainsMethod;
    public final int classContainsStaticMethod;
    public final int classContainsField;
    public final int classContainsStaticField;

    public final int self;

    public final BiFunction<Entity, Entity, Double> distanceCalculator;

    public PropertiesStrategy(int methodCallPublicMethod, int methodCallStaticMethod, int methodCallPrivateMethod,
                              int methodCalledByMethod, int methodUsePublicField, int methodUseStaticField,
                              int methodUsePrivateField, int methodUseClassMember, int methodContainedByClass,
                              int staticMethodContainedByClass, int fieldUsedByMethod, int fieldContainedByClass, int staticFieldUsedByMethod,
                              int staticFieldContainedByClass, int classContainsMethod, int classContainsStaticMethod,
                              int classContainsField, int classContainsStaticField, int self,
                              BiFunction<Entity, Entity, Double> distanceCalculator) {
        this.methodCallPublicMethod = methodCallPublicMethod;
        this.methodCallStaticMethod = methodCallStaticMethod;
        this.methodCallPrivateMethod = methodCallPrivateMethod;
        this.methodCalledByMethod = methodCalledByMethod;
        this.methodUsePublicField = methodUsePublicField;
        this.methodUseStaticField = methodUseStaticField;
        this.methodUsePrivateField = methodUsePrivateField;
        this.methodUseClassMember = methodUseClassMember;
        this.methodContainedByClass = methodContainedByClass;
        this.staticMethodContainedByClass = staticMethodContainedByClass;
        this.fieldUsedByMethod = fieldUsedByMethod;
        this.fieldContainedByClass = fieldContainedByClass;
        this.staticFieldUsedByMethod = staticFieldUsedByMethod;
        this.staticFieldContainedByClass = staticFieldContainedByClass;
        this.classContainsMethod = classContainsMethod;
        this.classContainsStaticMethod = classContainsStaticMethod;
        this.classContainsField = classContainsField;
        this.classContainsStaticField = classContainsStaticField;
        this.self = self;
        this.distanceCalculator = distanceCalculator;
    }

    public PropertiesStrategy(int[] values) {
        assert values.length == 20;
        this.methodCallPublicMethod = values[0];
        this.methodCallStaticMethod = values[1];
        this.methodCallPrivateMethod = values[2];
        this.methodCalledByMethod = values[3];
        this.methodUsePublicField = values[4];
        this.methodUseStaticField = values[5];
        this.methodUsePrivateField = values[6];
        this.methodUseClassMember = values[7];
        this.methodContainedByClass = values[8];
        this.staticMethodContainedByClass = values[9];
        this.fieldUsedByMethod = values[10];
        this.fieldContainedByClass = values[11];
        this.staticFieldUsedByMethod = values[12];
        this.staticFieldContainedByClass = values[13];
        this.classContainsMethod = values[14];
        this.classContainsStaticMethod = values[15];
        this.classContainsField = values[16];
        this.classContainsStaticField = values[17];
        this.self = values[18];
        this.distanceCalculator = DEFAULT_CALCULATORS.get(values[19]);
    }

    public int[] values() {
        return new int[]{methodCallPublicMethod, methodCallStaticMethod, methodCallPrivateMethod, methodCalledByMethod,
                methodUsePublicField, methodUseStaticField, methodUsePrivateField, methodUseClassMember,
                methodContainedByClass, staticMethodContainedByClass, fieldUsedByMethod, fieldContainedByClass,
                staticFieldUsedByMethod, staticFieldContainedByClass, classContainsMethod, classContainsStaticMethod,
                classContainsField, classContainsStaticField, self};
    }

    boolean acceptFile(final PsiFile file) {
        return file != null && file.getFileType().equals(JavaFileType.INSTANCE);
    }

    public boolean acceptClass(@NotNull PsiClass aClass) {
        return !(ClassUtils.isAnonymous(aClass) || aClass.getQualifiedName() == null
                || aClass.isEnum() || aClass.isInterface());
    }

    public boolean acceptMethod(@NotNull PsiMethod method) {
        if (method.isConstructor() || MethodUtils.isAbstract(method)) {
            return false;
        }
        final PsiClass containingClass = method.getContainingClass();
        return !(containingClass == null || containingClass.isInterface());
    }

    public boolean acceptField(@NotNull PsiField field) {
        return field.getContainingClass() != null;
    }

    @Override
    public String toString() {
        return "PropertiesStrategy{" +
                "methodCallPublicMethod=" + methodCallPublicMethod +
                ", methodCallStaticMethod=" + methodCallStaticMethod +
                ", methodCallPrivateMethod=" + methodCallPrivateMethod +
                ", methodCalledByMethod=" + methodCalledByMethod +
                ", methodUsePublicField=" + methodUsePublicField +
                ", methodUseStaticField=" + methodUseStaticField +
                ", methodUsePrivateField=" + methodUsePrivateField +
                ", methodUseClassMember=" + methodUseClassMember +
                ", methodContainedByClass=" + methodContainedByClass +
                ", staticMethodContainedByClass=" + staticMethodContainedByClass +
                ", fieldUsedByMethod=" + fieldUsedByMethod +
                ", fieldContainedByClass=" + fieldContainedByClass +
                ", staticFieldUsedByMethod=" + staticFieldUsedByMethod +
                ", staticFieldContainedByClass=" + staticFieldContainedByClass +
                ", classContainsMethod=" + classContainsMethod +
                ", classContainsStaticMethod=" + classContainsStaticMethod +
                ", classContainsField=" + classContainsField +
                ", classContainsStaticField=" + classContainsStaticField +
                ", self=" + self +
                ", distanceCalculator=" + getCalculatorName(distanceCalculator) +
                '}';
    }

    public String getCalculatorName(BiFunction<Entity, Entity, Double> calculator) {
        if (calculator == SIMPLE_MAX_CALCULATOR) {
            return "MAX_CALCULATOR";
        } else if (calculator == SIMPLE_MIN_CALCULATOR) {
            return "MIN_CALCULATOR";
        } else if (calculator == SIMPLE_AVERAGE_CALCULATOR) {
            return "AVERAGE_CALCULATOR";
        } else if (calculator == EXP_CALCULATOR) {
            return "EXP_CALCULATOR";
        }
        return "&&&";
    }
}

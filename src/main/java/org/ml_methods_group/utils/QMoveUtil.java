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

package org.ml_methods_group.utils;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;

import java.util.Map;
import java.util.stream.Stream;

public class QMoveUtil {
    public static void calculateRelatedClasses(PsiMethod method, Map<PsiClass, Integer> relatedClasses){
        PsiParameter[] parameters = method.getParameterList().getParameters();
        for (PsiParameter parameter : parameters) {
            PsiTypeElement typeElement = parameter.getTypeElement();
            if (typeElement == null) {
                continue;
            }
            PsiType type = typeElement.getType().getDeepComponentType();
            PsiClass classInType = PsiUtil.resolveClassInType(type);
            if (classInType == null) {
                continue;
            }
            incrementMapValue(classInType, relatedClasses);
        }
    }

    public static <T> int  removeFromUnion(Map<T, Integer> union, Map<T, Integer> set){
        int res = union.size();
        for(Map.Entry<T, Integer> item : set.entrySet()){
            int remove = item.getValue();
            T key = item.getKey();
            int present = union.get(key);
            if(remove == present){
                res--;
            }
        }
        return res;
    }

    public static <T> int addToUnion(Map<T, Integer> union, Map<T, Integer> set){
        int res = union.size();
        for(Map.Entry<T, Integer> item : set.entrySet()){
            int add = item.getValue();
            T key = item.getKey();
            if(!union.containsKey(key)){
                res++;
            }
        }
        return res;
    }

    public static <T> void incrementMapValue(T t, Map<T, Integer> map){
            map.put(t,
                    map.getOrDefault(t, 0) + 1);
    }

    public static void calculateMethodParameters(PsiMethod method, Map<PsiType, Integer> parameters){
        Stream.of(method.getParameterList()).
                flatMap(x -> Stream.of(x.getParameters())).
                map(PsiVariable::getType).forEach(x -> incrementMapValue(x, parameters));
    }
}

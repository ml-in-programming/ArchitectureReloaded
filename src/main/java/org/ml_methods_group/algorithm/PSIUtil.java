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

package org.ml_methods_group.algorithm;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.HashSet;
import java.util.Set;

public final class PSIUtil {
    private PSIUtil() {
    }

    public static Set<PsiClass> getAllSupers(PsiClass aClass, Set<PsiClass> allClasses) {
        final Set<PsiClass> allSupers = new HashSet<>();
        if (!allClasses.contains(aClass)) {
            return allSupers;
        }

        final PsiClass[] supers = aClass.getSupers();
        for (PsiClass superClass : supers) {
            if (!allClasses.contains(superClass)) {
                continue;
            }
            allSupers.add(superClass);
            allSupers.addAll(getAllSupers(superClass, allClasses));
        }

        return allSupers;
    }

    public static Set<PsiClass> getAllSupers(PsiClass aClass) {
        final Set<PsiClass> allSupers = new HashSet<>();
        final PsiClass[] supers = aClass.getSupers();

        for (PsiClass superClass : supers) {
            allSupers.add(superClass);
            allSupers.addAll(getAllSupers(superClass));
        }

        return allSupers;
    }

    public static Set<PsiMethod> getAllSupers(PsiMethod method, Set<PsiClass> allClasses) {
        if (!allClasses.contains(method.getContainingClass())) {
            return new HashSet<>();
        }

        final Set<PsiMethod> allSupers = new HashSet<>();
        final PsiMethod[] supers = method.findSuperMethods();

        for (PsiMethod superMethod : supers) {
            if (!allClasses.contains(superMethod.getContainingClass())) {
                continue;
            }
            allSupers.add(superMethod);
            allSupers.addAll(getAllSupers(superMethod, allClasses));
        }

        return allSupers;
    }

    public static Set<PsiMethod> getAllSupers(PsiMethod method) {
        final Set<PsiMethod> allSupers = new HashSet<>();
        final PsiMethod[] supers = method.findSuperMethods();

        for (PsiMethod superMethod : supers) {
            allSupers.add(superMethod);
            allSupers.addAll(getAllSupers(superMethod));
        }

        return allSupers;
    }

    public static boolean isOverriding(PsiMethod method) {
        return method.findSuperMethods().length != 0;
    }
}

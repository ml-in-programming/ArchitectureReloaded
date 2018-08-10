package org.jetbrains.research.groups.ml_methods.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
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

    public static @NotNull Optional<PsiMethod> getParentMethod(final @NotNull PsiElement psiElement) {
        PsiMethod parent = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
        if (parent == null) {
            return Optional.empty();
        }

        return Optional.of(parent);
    }
}

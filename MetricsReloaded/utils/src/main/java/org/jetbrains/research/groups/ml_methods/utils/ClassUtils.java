package org.jetbrains.research.groups.ml_methods.utils;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ClassUtils {
    public static @NotNull Set<PsiClass> getMeaningfulClasses(final @NotNull PsiClass mainClass) {
        Set<PsiClass> meaningfulClasses = new HashSet<>();

        mainClass.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);
                meaningfulClasses.addAll(MethodUtils.getMeaningfulClasses(method));
            }

            @Override
            public void visitClass(PsiClass aClass) {
                if (!aClass.equals(mainClass)) {
                    return;
                }

                super.visitClass(aClass);
            }
        });

        return meaningfulClasses;
    }
}

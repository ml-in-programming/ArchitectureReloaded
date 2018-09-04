/*
 * Copyright 2005-2016 Sixth and Red River Software, Bas Leijdekkers
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.metrics.utils;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.SuperMethodsSearch;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public final class MethodUtils {
    private static final @NotNull Logger LOGGER = Logger.getLogger(MethodUtils.class);
    private static List<String> getterPrefixes = Arrays.asList("get", "is", "has");

    private MethodUtils() {}

    public static boolean isConcreteMethod(PsiMethod method) {
        return method != null && !method.isConstructor() && !method.hasModifierProperty(PsiModifier.ABSTRACT) &&
                !method.hasModifierProperty(PsiModifier.STATIC) && !method.hasModifierProperty(PsiModifier.PRIVATE);
    }

    public static boolean hasConcreteSuperMethod(PsiMethod method) {
        final Query<MethodSignatureBackedByPsiMethod> search = SuperMethodsSearch.search(method, null, true, false);
        return !search.forEach(new Processor<MethodSignatureBackedByPsiMethod>() {

            @Override
            public boolean process(MethodSignatureBackedByPsiMethod superMethod) {
                return isAbstract(superMethod.getMethod());
            }
        });
    }

    public static boolean isAbstract(PsiMethod method) {
        if (method.hasModifierProperty(PsiModifier.STATIC) || method.hasModifierProperty(PsiModifier.DEFAULT)) {
            return false;
        }
        if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
            return true;
        }
        final PsiClass containingClass = method.getContainingClass();
        return containingClass != null && containingClass.isInterface();
    }

    public static boolean isStatic(PsiModifierListOwner unit) {
        return unit.hasModifierProperty(PsiModifier.STATIC);
    }

    public static boolean isSynchronized(PsiModifierListOwner unit) {
        return unit.hasModifierProperty(PsiModifier.SYNCHRONIZED);
    }

    public static boolean isOverriding(PsiMethod method) {
        return method.findSuperMethods().length != 0;
    }

    public static boolean isPrivate(PsiModifierListOwner unit) {
        return unit.hasModifierProperty(PsiModifier.PRIVATE);
    }

    public static boolean isPublic(PsiModifierListOwner unit) {
        return unit.hasModifierProperty(PsiModifier.PUBLIC);
    }

    public static boolean isProtected(PsiModifierListOwner unit) {
        return unit.hasModifierProperty(PsiModifier.PROTECTED);
    }

    public static boolean isPackagePrivate(PsiModifierListOwner unit) {
        return unit.hasModifierProperty(PsiModifier.PACKAGE_LOCAL);
    }

    public static boolean isFinal(PsiModifierListOwner unit) {
        return unit.hasModifierProperty(PsiModifier.FINAL);
    }

    public static boolean isVolatile(PsiModifierListOwner unit) {
        return unit.hasModifierProperty(PsiModifier.VOLATILE);
    }

    public static int parametersCount(PsiMethod method) {
        return method.getParameterList().getParametersCount();
    }

    private static String calculateSignature(PsiMethod method, boolean isCanonical) {
        final PsiClass containingClass = method.getContainingClass();
        final String className;
        if (containingClass != null) {
            className = containingClass.getQualifiedName();
        } else {
            className = "";
        }
        final String methodName = method.getName();
        final StringBuilder out = new StringBuilder(50);
        out.append(className);
        out.append('.');
        out.append(methodName);
        out.append('(');
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                out.append(',');
            }
            final PsiType parameterType = parameters[i].getType();
            final String parameterTypeText = isCanonical ?
                    parameterType.getCanonicalText() : parameterType.getPresentableText();
            out.append(parameterTypeText);
        }
        out.append(')');
        return out.toString();
    }

    public static String calculateUniqueSignature(PsiMethod method) {
        return calculateSignature(method, true);
    }

    public static String calculateHumanReadableSignature(PsiMethod method) {
        return calculateSignature(method, false);
    }

    public static boolean isGetter(final PsiMethod method) {
        final PsiType returnType = method.getReturnType();
        return !PsiType.VOID.equals(returnType)
                && parametersCount(method) == 0
                && isGetterName(method);
    }

    private static boolean isGetterName(final PsiMethod method) {
        if (method == null || method.getContainingClass() == null) {
            return false;
        }
        final String name = method.getName();
        final String lowerCase = name.toLowerCase();
        Optional<Boolean> res = getterPrefixes.stream()
                .map(p -> lowerCase.length() > p.length() && lowerCase.startsWith(p))
                .reduce(Boolean::logicalOr);
        if (res.isPresent() && res.get()) {
            return true;
        }
        final PsiClass aClass = method.getContainingClass();
        res = Arrays.stream(aClass.getAllFields())
                .filter(f -> f.getName() != null)
                .map(f -> f.getName().toLowerCase().equals(lowerCase))
                .reduce(Boolean::logicalOr);
        return res.isPresent() && res.get();
    }

    public static boolean isTrivialGetter(final PsiMethod method) {
        if (!isGetter(method)) {
            return false;
        }
        final PsiCodeBlock body = method.getBody();
        if (body == null) {
            return false;
        }
        final PsiStatement[] statements = body.getStatements();
        return statements.length == 1 && statements[0] instanceof PsiReturnStatement
                && getUsedFields(statements[0]).size() == 1;
    }

    public static boolean isSetter(final PsiMethod method) {
        final String methodName = method.getName();
        final PsiType returnType = method.getReturnType();
        final PsiParameterList params = method.getParameterList();
        if (!PsiType.VOID.equals(returnType) || !methodName.toLowerCase().startsWith("set")
                || params.getParametersCount() != 1) {
            return false;
        }
        final PsiClass aClass = method.getContainingClass();
        return aClass != null;
    }

    public static boolean isTrivialSetter(final PsiMethod method) {
        if (!isSetter(method)) {
            return false;
        }
        final PsiCodeBlock body = method.getBody();
        if (body == null) {
            return false;
        }
        final PsiStatement[] statements = body.getStatements();
        return statements.length == 1 && statements[0] instanceof PsiExpressionStatement
                && getUsedFields(method).size() == 1;
    }

    @NotNull
    public static Set<PsiField> getUsedFields(@NotNull final PsiElement element) {
        final Set<PsiField> fields = new HashSet<PsiField>();
        element.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);
                final PsiElement element = expression.resolve();
                if (element instanceof PsiField) {
                    fields.add((PsiField) element);
                }
            }
        });
        return fields;
    }

    public static boolean isGetterOrSetter(final PsiMethod method) {
        return isGetter(method) || isSetter(method);
    }

    public static boolean isTrivialGetterOrSetter(final PsiMethod method) {
        return isTrivialGetter(method) || isTrivialSetter(method);
    }

    private static boolean hasField(@NotNull final PsiClass aClass,
                                    @NotNull final String name,
                                    @NotNull final PsiType type) {
        final PsiField[] fields = aClass.getAllFields();
        for (final PsiField field : fields) {
            final String fieldName = field.getName();
            if (fieldName != null && fieldName.toLowerCase().equals(name.toLowerCase())
                    && type.isAssignableFrom(field.getType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPublic(PsiMethod method) {
        if (method.hasModifierProperty(PsiModifier.PUBLIC)) {
            return true;
        }

        PsiClass containingClass = method.getContainingClass();

        return containingClass != null && containingClass.isInterface();
    }

    public static String extractMethodDeclaration(final @NotNull PsiMethod method) {
        String code = method.getText();

        code = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(code).replaceAll("");
        code = Pattern.compile("//.*?$", Pattern.DOTALL | Pattern.MULTILINE).matcher(code).replaceAll("");

        code = Pattern.compile("\\{.*}", Pattern.DOTALL).matcher(code).replaceAll("");
        return code.trim();
    }

    /**
     * This method looks for not overriding methods, located in super or containing class and which are overloads of given method (have the same name).
     * It doesn't exclude from return set the original passed method (or its deepest super method).
     * @param method whose overloads we need to find.
     * @param containingClass class where method is located.
     * @param considerSupers true if we need look into supers.
     * @return all methods that are not overriding, located in supers or in containingClass and have the same name as method.
     */
    public static List<PsiMethod> getAllRootOverloads(@NotNull PsiMethod method, @NotNull PsiClass containingClass,
                                                     boolean considerSupers) {
        String methodName = method.getName();
        List<PsiMethod> overloads = new ArrayList<>();
        for (PsiMethod methodInClass : containingClass.getMethods()) {
            if (methodInClass.getName().equals(methodName) && !isOverriding(methodInClass)) {
                overloads.add(methodInClass);
            }
        }
        if (considerSupers) {
            processSupers(method, containingClass, overloads);
        }
        return overloads;
    }

    private static void processSupers(@NotNull PsiMethod method, @NotNull PsiClass containingClass, @NotNull List<PsiMethod> overloads) {
        for (PsiClass superClass : containingClass.getSupers()) {
            for (PsiMethod methodInClass : superClass.getMethods()) {
                if (methodInClass.getName().equals(method.getName()) && !isOverriding(methodInClass)) {
                    overloads.add(methodInClass);
                }
            }
            processSupers(method, superClass, overloads);
        }
    }

    public static List<PsiMethod> getOverloads(@NotNull PsiMethod method, @NotNull PsiClass containingClass,
                                           boolean considerSupers) {
        List<PsiMethod> overloads = getAllRootOverloads(method, containingClass, considerSupers);
        PsiMethod methodThatWasAsked = isOverriding(method) ? method.findDeepestSuperMethods()[0] : method;
        if (overloads.stream()
                .filter(method1 -> calculateSignature(methodThatWasAsked).equals(calculateSignature(method1)))
                .count() != 1) {
            String errorMessage = "Set of overloaded methods must contain original method.\n" +
                    "Overloads was searched for " + calculateSignature(method);
            LOGGER.error(errorMessage);
        }
        return overloads;
    }

    public static int getNumberOfOverloads(@NotNull PsiMethod method, @NotNull PsiClass containingClass,
                                           boolean considerSupers) {
        return getOverloads(method, containingClass, considerSupers).size();
    }

    public static boolean isGeneric(PsiMethod method) {
        return method.getTypeParameters().length != 0;
    }

    public static @NotNull Set<PsiClass> getMeaningfulClasses(final @NotNull PsiMethod mainMethod) {
        Set<PsiClass> meaningfulClasses = new HashSet<>();

        mainMethod.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                if (!method.equals(mainMethod)) {
                    return;
                }

                super.visitMethod(method);

                convertToClass(method.getReturnType()).ifPresent(meaningfulClasses::add);

                for (PsiClassType classType : method.getThrowsList().getReferencedTypes()) {
                    PsiClass psiClass = classType.resolve();
                    if (psiClass != null) {
                        meaningfulClasses.add(psiClass);
                    }
                }
            }

            @Override
            public void visitClass(PsiClass aClass) {}

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);

                PsiMethod calledMethod = expression.resolveMethod();
                if (calledMethod == null) {
                    return;
                }

                PsiClass containingClass = calledMethod.getContainingClass();
                if (containingClass != null) {
                    meaningfulClasses.add(containingClass);
                }
            }

            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);

                JavaResolveResult result = expression.advancedResolve(false);
                PsiElement element = result.getElement();

                if (!(element instanceof PsiField)) {
                    return;
                }

                PsiField field = (PsiField) element;

                convertToClass(field.getType()).ifPresent(meaningfulClasses::add);
            }

            @Override
            public void visitNewExpression(PsiNewExpression exp) {
                super.visitNewExpression(exp);

                PsiJavaCodeReferenceElement classReference = exp.getClassReference();
                if (classReference == null) {
                    return;
                }

                PsiClass psiClass = (PsiClass) classReference.resolve();
                if (psiClass != null) {
                    meaningfulClasses.add(psiClass);
                }
            }

            @Override
            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                super.visitDeclarationStatement(statement);

                for (PsiElement element : statement.getDeclaredElements()) {
                    if (element instanceof PsiLocalVariable) {
                        PsiLocalVariable localVariable = (PsiLocalVariable) element;
                        convertToClass(localVariable.getTypeElement().getType()).ifPresent(meaningfulClasses::add);
                    }
                }
            }

            private @NotNull Optional<PsiClass> convertToClass(final @Nullable PsiType type) {
                if (type == null) {
                    return Optional.empty();
                }

                if (!(type instanceof PsiClassType)) {
                    return Optional.empty();
                }

                return Optional.ofNullable(((PsiClassType) type).resolve());
            }
        });

        return meaningfulClasses;
    }
}

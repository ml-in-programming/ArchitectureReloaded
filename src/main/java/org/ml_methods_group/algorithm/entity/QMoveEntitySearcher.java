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

package org.ml_methods_group.algorithm.entity;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.utils.MethodUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.properties.finder_strategy.FinderStrategy;
import org.ml_methods_group.algorithm.properties.finder_strategy.NewStrategy;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.PSIUtil;

import java.util.*;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public class QMoveEntitySearcher  {
    private static final Logger LOGGER = Logging.getLogger(QMoveEntitySearcher.class);

    private final Map<String, PsiClass> classForName = new HashMap<>();
    private final Map<PsiElement, Entity> entities = new HashMap<>();
    private final AnalysisScope scope;
    private final long startTime;
    private final FinderStrategy strategy;
    private final ProgressIndicator indicator;

    private QMoveEntitySearcher(AnalysisScope scope) {
        this.scope = scope;
        strategy = NewStrategy.getInstance();
        startTime = System.currentTimeMillis();
        if (ProgressManager.getInstance().hasProgressIndicator()) {
            indicator = ProgressManager.getInstance().getProgressIndicator();
        } else {
            indicator = new EmptyProgressIndicator();
        }
    }

    public static QMoveEntitySearchResult analyze(AnalysisScope scope, MetricsRun metricsRun) {
        final QMoveEntitySearcher finder = new QMoveEntitySearcher(scope);
        return finder.runCalculations(metricsRun);
    }

    private QMoveEntitySearchResult runCalculations(MetricsRun metricsRun) {
        indicator.pushState();
        indicator.setText("Searching entities");
        indicator.setIndeterminate(true);
        LOGGER.info("Indexing entities...");
        scope.accept(new QMoveEntitySearcher.UnitsFinder());
        indicator.setIndeterminate(false);
        LOGGER.info("Calculating properties...");
        indicator.setText("Calculating properties");
        scope.accept(new QMoveEntitySearcher.PropertiesCalculator());
        indicator.popState();
        return prepareResult(metricsRun);
    }

    private QMoveEntitySearchResult prepareResult(MetricsRun metricsRun) {
        LOGGER.info("Preparing results...");
        final List<ClassEntity> classes = new ArrayList<>();
        final List<MethodEntity> methods = new ArrayList<>();
        final List<FieldEntity> fields = new ArrayList<>();
        final List<Entity> validEntities = new ArrayList<>();
        for (Entity entity : entities.values()) {
            indicator.checkCanceled();
            try {
                entity.calculateVector(metricsRun);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn("Failed to calculate vector for " + entity.getName());
                continue;
            }
            validEntities.add(entity);
            switch (entity.getCategory()) {
                case Class:
                    classes.add((ClassEntity) entity);
                    break;
                case Method:
                    methods.add((MethodEntity) entity);
                    break;
                default:
                    fields.add((FieldEntity) entity);
                    break;
            }
        }
        LOGGER.info("Properties calculated");
        LOGGER.info("Generated " + classes.size() + " class entities");
        LOGGER.info("Generated " + methods.size() + " method entities");
        LOGGER.info("Generated " + fields.size() + " field entities");
        return new QMoveEntitySearchResult(classes, methods, fields, System.currentTimeMillis() - startTime, scope);
    }

    private class UnitsFinder extends JavaRecursiveElementVisitor {

        @Override
        public void visitFile(PsiFile file) {
            indicator.checkCanceled();
            if (strategy.acceptFile(file)) {
                LOGGER.info("Indexing " + file.getName());
                super.visitFile(file);
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            indicator.checkCanceled();
            classForName.put(getHumanReadableName(aClass), aClass);
            if (!strategy.acceptClass(aClass)) {
                return;
            }
            entities.put(aClass, new QMoveClassEntity(aClass));
            super.visitClass(aClass);
        }

        @Override
        public void visitField(PsiField field) {
            if (!strategy.acceptField(field)) {
                return;
            }
            indicator.checkCanceled();
            entities.put(field, new FieldEntity(field));
            super.visitField(field);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            if (!strategy.acceptMethod(method)) {
                return;
            }
            indicator.checkCanceled();
            entities.put(method, new QMoveMethodEntity(method));
            super.visitMethod(method);
        }
    }

    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private int propertiesCalculated = 0;

        private PsiMethod currentMethod;

        @Override
        public void visitFile(PsiFile file) {
            indicator.checkCanceled();
            if (strategy.acceptFile(file)) {
                super.visitFile(file);
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            indicator.checkCanceled();
            final QMoveClassEntity entity = (QMoveClassEntity) entities.get(aClass);
            if (entity == null) {
                super.visitClass(aClass);
                return;
            }
            entity.getProperties().addToRelatedClasses(aClass);
            for(PsiClass superClass : PSIUtil.getAllSupers(aClass)){
                if(isClassInProject(superClass)){
                    QMoveClassEntity superClassEntity = (QMoveClassEntity) entities.get(superClass);
                    if(superClassEntity == null){
                        continue;
                    }
                    superClassEntity.getProperties().getInheritors().add(entity);
                    entity.getProperties().getSupers().add(superClassEntity);
                }
            }

            for(PsiClass innerClass : aClass.getInnerClasses()){
                if(isClassInProject(innerClass)){
                    QMoveClassEntity innerClassEntity = (QMoveClassEntity) entities.get(innerClass);
                    if(innerClassEntity == null){
                        continue;
                    }
                    entity.getProperties().getInnerClasses().add(innerClassEntity);
                    innerClassEntity.getProperties().getOuterClasses().add(entity);
                }
            }
            reportPropertiesCalculated();
            super.visitClass(aClass);
        }

        private boolean isProperty(PsiClass aClass, PsiMember member) {
            return !(member instanceof PsiMethod && ((PsiMethod) member).isConstructor()) && (aClass.equals(member.getContainingClass()) || !MethodUtils.isPrivate(member));
        }

        @Contract("null -> false")
        private boolean isClassInProject(final @Nullable PsiClass aClass) {
            return aClass != null && classForName.containsKey(getHumanReadableName(aClass));
        }

        @Override
        public void visitMethod(PsiMethod method) {
            indicator.checkCanceled();
            final QMoveMethodEntity entity = (QMoveMethodEntity) entities.get(method);
            if (entity == null) {
                super.visitMethod(method);
                return;

            }
            if (currentMethod == null) {
                currentMethod = method;
            }
            QMoveClassEntity containingClass = (QMoveClassEntity) entities.get(method.getContainingClass());
            if(containingClass != null){
                entity.setContainingClass(containingClass);
                containingClass.getProperties().incrementNumOfMethods();
                for(PsiParameter parameter : method.getParameterList().getParameters()){
                    PsiTypeElement typeElement = parameter.getTypeElement();
                    if (typeElement == null) {
                        continue;
                    }
                    PsiType type = typeElement.getType().getDeepComponentType();
                    //parameters of methods for cohesion
                    entity.getProperties().addToParameters(type);
                    containingClass.getProperties().addToParameters(type);

                    PsiClass classInType = PsiUtil.resolveClassInType(type);
                    if (classInType == null) {
                        continue;
                    }
                    //coupling
                    entity.getProperties().addToRelatedClasses(classInType);
                    containingClass.getProperties().addToRelatedClasses(classInType);
                    //composition
                    if(isClassInProject(classInType)){
                        containingClass.getProperties().incrementUserDefinedClasses();
                    }
                }
                //cohesion
                int sumIntersection = entity.getProperties().setMethodSumIntersection();
                containingClass.getProperties().increaseSumIntersection(sumIntersection);

                entity.getProperties().setInnerClasses(
                        containingClass.getProperties().getInnerClasses());

                entity.getProperties().setInheritors(
                        containingClass.getProperties().getInheritors());

                entity.getProperties().setOuterClasses(
                        containingClass.getProperties().getOuterClasses());

                entity.getProperties().setSupers(
                        containingClass.getProperties().getSupers());
            }
            reportPropertiesCalculated();
            super.visitMethod(method);
            if (currentMethod == method) {
                currentMethod = null;
            }
        }

        @Override
        public void visitField(PsiField field) {
            indicator.checkCanceled();
            final Entity entity = entities.get(field);
            if (entity == null) {
                super.visitField(field);
                return;
            }

            final PsiClass containingClass = field.getContainingClass();
            if (containingClass != null) {
                final PsiClass fieldClass = PsiUtil.resolveClassInType(field.getType());
                if(fieldClass != null){
                    ((QMoveClassEntity)entities.get(containingClass))
                            .getProperties().addToRelatedClasses(fieldClass);
                }
                if (isClassInProject(fieldClass)) {
                    ((QMoveClassEntity)entities.get(containingClass))
                            .getProperties().incrementUserDefinedClasses();
                }
            }
            reportPropertiesCalculated();
            super.visitField(field);
        }


        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            indicator.checkCanceled();
            PsiElement element = expression.resolve();
            if (currentMethod != null && element instanceof PsiField) {
                final PsiField field = (PsiField) element;
                if(field.hasModifierProperty(PsiModifier.PRIVATE)){
                    ((QMoveMethodEntity)entities.get(currentMethod))
                            .getProperties().setContainsPrivateCalls(true);
                }

                if(field.hasModifierProperty(PsiModifier.PROTECTED)){
                    ((QMoveMethodEntity)entities.get(currentMethod))
                            .getProperties().setContainsProtectedCalls(true);
                }
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            indicator.checkCanceled();
            final PsiMethod called = expression.resolveMethod();
            final PsiClass usedClass = called != null ? called.getContainingClass() : null;
            if (currentMethod != null && called != null) {
                if(called.hasModifierProperty(PsiModifier.PRIVATE)){
                    ((QMoveMethodEntity)entities.get(currentMethod))
                            .getProperties().setContainsPrivateCalls(true);
                }

                if(called.hasModifierProperty(PsiModifier.PROTECTED)){
                    ((QMoveMethodEntity)entities.get(currentMethod))
                            .getProperties().setContainsProtectedCalls(true);
                }

            }
            super.visitMethodCallExpression(expression);
        }

        private void reportPropertiesCalculated() {
            propertiesCalculated++;
            if (indicator != null) {
                indicator.setFraction((double) propertiesCalculated / entities.size());
            }
        }
    }

    private Optional<RelevantProperties> propertiesFor(PsiElement element) {
        return Optional.ofNullable(entities.get(element))
                .map(Entity::getRelevantProperties);
    }

}
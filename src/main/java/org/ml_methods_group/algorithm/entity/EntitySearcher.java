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

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.AnonymousClassElement;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.utils.MethodUtils;
import org.apache.log4j.Logger;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.PSIUtil;

import java.util.*;

import static org.ml_methods_group.utils.PsiSearchUtil.getHumanReadableName;

public class EntitySearcher {

    private static final Logger LOGGER = Logging.getLogger(EntitySearcher.class);

    private final Map<String, PsiClass> classForName = new HashMap<>();
    private final Map<PsiElement, Entity> entities = new HashMap<>();
    private final AnalysisScope scope;
    private final long startTime;

    private EntitySearcher(AnalysisScope scope) {
        this.scope = scope;
        startTime = System.currentTimeMillis();
    }

    public static EntitySearchResult analyze(AnalysisScope scope, MetricsRun metricsRun) {
        final EntitySearcher finder = new EntitySearcher(scope);
        return finder.runCalculations(metricsRun);
    }

    private EntitySearchResult runCalculations(MetricsRun metricsRun) {
        final ProgressIndicator indicator;
        if (ProgressManager.getInstance().hasProgressIndicator()) {
            indicator = ProgressManager.getInstance().getProgressIndicator();
        } else {
            indicator = new EmptyProgressIndicator();
        }
        indicator.pushState();
        indicator.setText("Search units");
        indicator.setIndeterminate(true);
        LOGGER.info("Index units...");
        scope.accept(new UnitsFinder());
        indicator.setIndeterminate(false);
        LOGGER.info("Calculate properties...");
        indicator.setText("Calculate properties");
        scope.accept(new PropertiesCalculator(indicator));
        indicator.popState();
        return prepareResult(metricsRun);
    }

    private EntitySearchResult prepareResult(MetricsRun metricsRun) {
        LOGGER.info("Prepare results...");
        final List<ClassEntity> classes = new ArrayList<>();
        final List<MethodEntity> methods = new ArrayList<>();
        final List<FieldEntity> fields = new ArrayList<>();
        final List<Entity> validEntities = new ArrayList<>();
        for (Entity entity : entities.values()) {
            try {
                entity.calculateVector(metricsRun);
            } catch (Exception e) {
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
        Entity.normalize(validEntities);
        LOGGER.info("Properties calculated");
        LOGGER.info("Generated " + classes.size() + " class entities");
        LOGGER.info("Generated " + methods.size() + " method entities");
        LOGGER.info("Generated " + fields.size() + " field entities");
        return new EntitySearchResult(classes, methods, fields, System.currentTimeMillis() - startTime);
    }

    private class UnitsFinder extends JavaRecursiveElementVisitor {

        @Override
        public void visitFile(PsiFile file) {
            if (isSourceFile(file)) {
                LOGGER.info("Index " + file.getName());
                super.visitFile(file);
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            classForName.put(getHumanReadableName(aClass), aClass);
            if (aClass.isEnum()) {
                return;
            }
            if (aClass instanceof AnonymousClassElement) {
                return;
            }

            if (aClass.getQualifiedName() == null) {
                return;
            }
            entities.put(aClass, new ClassEntity(aClass));
            super.visitClass(aClass);
        }

        @Override
        public void visitField(PsiField field) {
            entities.put(field, new FieldEntity(field));
            super.visitField(field);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            entities.put(method, new MethodEntity(method));
            super.visitMethod(method);
        }
    }

    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private final ProgressIndicator indicator;
        private int propertiesCalculated = 0;

        private PropertiesCalculator(ProgressIndicator indicator) {
            this.indicator = indicator;
        }

        private PsiMethod currentMethod;

        @Override
        public void visitFile(PsiFile file) {
            if (isSourceFile(file)) {
                super.visitFile(file);
            }
        }

        @Override
        public void visitClass(PsiClass aClass) {
            final Entity entity = entities.get(aClass);
            if (entity == null) {
                super.visitClass(aClass);
                return;
            }
            final RelevantProperties classProperties = entity.getRelevantProperties();
            classProperties.addClass(aClass);
            for (PsiClass superClass : PSIUtil.getAllSupers(aClass)) {
                if (superClass.isInterface()) {
                    classProperties.addClass(superClass);
                } else {
                    propertiesFor(superClass).ifPresent(p -> p.addClass(aClass));
                }
            }
            Arrays.stream(aClass.getAllMethods())
                    .filter(m -> isProperty(aClass, m))
                    .forEach(classProperties::addMethod);
            Arrays.stream(aClass.getAllFields())
                    .filter(m -> isProperty(aClass, m))
                    .forEach(classProperties::addField);
            reportPropertiesCalculated();
            super.visitClass(aClass);
        }

        private boolean isProperty(PsiClass aClass, PsiMember member) {
            return aClass.equals(member.getContainingClass()) || !MethodUtils.isPrivate(member);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            final Entity entity = entities.get(method);
            if (entity == null) {
                super.visitMethod(method);
                return;

            }
            final RelevantProperties methodProperties = entity.getRelevantProperties();
            methodProperties.addMethod(method);
            Optional.ofNullable(method.getContainingClass())
                    .ifPresent(methodProperties::addClass);
            if (currentMethod == null) {
                currentMethod = method;
            }
            PSIUtil.getAllSupers(method).stream()
                    .map(entities::get)
                    .filter(Objects::nonNull)
                    .map(Entity::getRelevantProperties)
                    .forEach(properties -> properties.addOverrideMethod(method));
            Arrays.stream(method.getParameterList().getParameters())
                    .map(PsiParameter::getType)
                    .map(PsiType::getCanonicalText)
                    .map(classForName::get)
                    .filter(Objects::nonNull)
                    .forEach(methodProperties::addClass);
            reportPropertiesCalculated();
            super.visitMethod(method);
            if (currentMethod == method) {
                currentMethod = null;
            }
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            PsiElement element = expression.resolve();
            if (currentMethod != null && element instanceof PsiField) {
                propertiesFor(currentMethod)
                        .ifPresent(p -> p.addField((PsiField) element));
                propertiesFor(element)
                        .ifPresent(p -> p.addMethod(currentMethod));
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitField(PsiField field) {
            final Entity entity = entities.get(field);
            if (entity == null) {
                super.visitField(field);
                return;
            }
            RelevantProperties fieldProperties = entity.getRelevantProperties();
            fieldProperties.addField(field);
            final PsiClass containingClass = field.getContainingClass();
            if (containingClass != null) {
                fieldProperties.addClass(containingClass);
            }
            reportPropertiesCalculated();
            super.visitField(field);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            PsiElement element = expression.getMethodExpression().resolve();
            if (currentMethod != null && element instanceof PsiMethod) {
                propertiesFor(currentMethod)
                        .ifPresent(p -> p.addMethod((PsiMethod) element));
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

    private boolean isSourceFile(PsiFile file) {
        return file.getName().endsWith(".java");
    }
}

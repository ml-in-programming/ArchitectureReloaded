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

package org.ml_methods_group.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.metricModel.TimeStamp;
import com.sixrr.metrics.profile.MetricsProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.*;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.EntitySearcher;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RefactoringExecutionContext {
    private static final List<Class<? extends Algorithm>> ALGORITHMS = Arrays.asList(ARI.class, AKMeans.class,
            CCDA.class, HAC.class, MRI.class);

    @NotNull
    private final MetricsRunImpl metricsRun = new MetricsRunImpl();
    private final Project project;
    private final AnalysisScope scope;
    @NotNull
    private final MetricsProfile profile;
    private EntitySearchResult entitySearchResult;
    private final MetricsExecutionContextImpl metricsExecutionContext;
    @Nullable
    private final Consumer<RefactoringExecutionContext> continuation;
    @NotNull
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<AlgorithmResult> algorithmsResults = new ArrayList<>();
    @NotNull
    private final Collection<String> requestedAlgorithms;

    public RefactoringExecutionContext(@NotNull Project project, @NotNull AnalysisScope scope,
                                       @NotNull MetricsProfile profile,
                                       @Nullable Consumer<RefactoringExecutionContext> continuation) {
        this(project, scope, profile, Arrays.asList(getAvailableAlgorithms()), continuation);
    }

    public RefactoringExecutionContext(@NotNull Project project, @NotNull AnalysisScope scope,
                                       @NotNull MetricsProfile profile,
                                       @NotNull Collection<String> requestedAlgorithms,
                                       @Nullable Consumer<RefactoringExecutionContext> continuation) {
        this.project = project;
        this.scope = scope;
        this.profile = profile;
        this.continuation = continuation;
        this.requestedAlgorithms = requestedAlgorithms;
        metricsExecutionContext = new MetricsExecutionContextImpl(project, scope);
    }

    public void executeAsync() {
        Task.Modal task = new Task.Modal(project, "Search For Refactorings", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                execute();
            }

            @Override
            public void onSuccess() {
                RefactoringExecutionContext.this.onFinish();
            }
        };
        task.queue();
    }

    public void executeSynchronously() {
        execute();
        onFinish();
    }

    private void execute() {
        metricsExecutionContext.calculateMetrics(profile, metricsRun);
        metricsRun.setProfileName(profile.getName());
        metricsRun.setContext(scope);
        metricsRun.setTimestamp(new TimeStamp());
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        final int tasks = requestedAlgorithms.size() + 1;
        int completedTasks = 0;
        indicator.setText("Generate entities");
        indicator.setFraction(completedTasks / tasks);
        entitySearchResult = ApplicationManager.getApplication()
                .runReadAction((Computable<EntitySearchResult>) () -> EntitySearcher.analyze(scope, metricsRun));
        completedTasks++;
        for (String algorithm : requestedAlgorithms) {
            indicator.setText("Run algorithm " + algorithm);
            indicator.setFraction((double) completedTasks / tasks);
            calculateAlgorithmForName(algorithm);
            completedTasks++;
        }
    }


    private void onFinish() {
        System.out.println("Classes: " + getClassCount());
        System.out.println("Methods: " + getMethodsCount());
        System.out.println("Fields: " + getFieldsCount());
        System.out.println("Total properties: " + entitySearchResult.getPropertiesCount());
        System.out.println();

        if (continuation != null) {
            continuation.accept(this);
        }
    }

    private static Algorithm createInstance(Class<? extends Algorithm> algorithmClass) {
        try {
            return algorithmClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of algorithm", e);
        }
    }

    private void calculate(Class<? extends Algorithm> algorithmClass) {
        final Algorithm algorithm = createInstance(algorithmClass);
        System.out.println("Starting " + algorithmClass.getSimpleName() + "...");
        final AlgorithmResult result = algorithm.execute(entitySearchResult, executorService);
        final Map<String, String> refactorings = result.getRefactorings();
        System.out.println("Finished " + algorithmClass.getSimpleName() + "\n");
        for (String ent : refactorings.keySet()) {
            System.out.println(ent + " --> " + refactorings.get(ent));
        }
        algorithmsResults.add(result);
    }

    private void calculateAlgorithmForName(String algorithm) {
        for (Class<? extends Algorithm> algorithmClass : ALGORITHMS) {
            if (algorithm.equals(algorithmClass.getSimpleName())) {
                ApplicationManager.getApplication()
                        .runReadAction(() -> calculate(algorithmClass));
                return;
            }
        }
        throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
    }

    public List<AlgorithmResult> getAlgorithmResults() {
        return new ArrayList<>(algorithmsResults);
    }

    public EntitySearchResult getEntitySearchResult() {
        return entitySearchResult;
    }

    public int getClassCount() {
        return entitySearchResult.getClasses().size();
    }

    public int getMethodsCount() {
        return entitySearchResult.getMethods().size();
    }

    public int getFieldsCount() {
        return entitySearchResult.getFields().size();
    }

    public Project getProject() {
        return project;
    }

    public AnalysisScope getScope() {
        return scope;
    }

    @NotNull
    public MetricsRunImpl getMetricsRun() {
        return metricsRun;
    }

    @NotNull
    public MetricsProfile getProfile() {
        return profile;
    }

    public static String[] getAvailableAlgorithms() {
        return ALGORITHMS.stream()
                .map(Class::getSimpleName)
                .toArray(String[]::new);
    }
}

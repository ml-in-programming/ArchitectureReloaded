/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
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

package vector.model;

import java.util.*;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.*;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.AbstractNumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.AnderbergHierarchicalClustering;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

/**
 * Created by Kivi on 09.05.2017.
 */
public class ARI {

    public ARI(List<Entity> entityList, Set<PsiClass> existingClasses) {
        entities = entityList;
        allClasses = existingClasses;
    }

    public Map<String, String> run() {
        Map<String, String> refactorings = new HashMap<String, String>();
        for (Entity entity : entities) {
            if (entity.getCategory().equals(MetricCategory.Method)) {
                String className = entity.getClassName();
                double minDist = Double.MAX_VALUE;
                int idClass = -1;
                for (int i = 0; i < entities.size(); ++i) {
                    Entity classEnt = entities.get(i);
                    if (classEnt.getCategory().equals(MetricCategory.Class)) {
                        double dist = entity.dist(classEnt);
                        if (dist < minDist) {
                            minDist = dist;
                            idClass = i;
                        }
                    }
                }

                if (idClass == -1) {
                    System.out.println("WARNING: " + entity.getName() + " has no nearest class");
                } else {
                    if (entities.get(idClass).getName().equals(className)) {
                        continue;
                    }

                    PsiMethod method = (PsiMethod) entity.getPsiElement();
                    PsiClass moveFromClass = method.getContainingClass();
                    PsiClass moveToClass = (PsiClass) entities.get(idClass).getPsiElement();

                    Set<PsiClass> supersTo = PSIUtil.getAllSupers(moveToClass, allClasses);//new HashSet<PsiClass>(Arrays.asList(moveToClass.getSupers()));
                    boolean isSuper = false;

                    for (PsiClass sup : supersTo) {
                        if (sup.equals(moveFromClass)) {
                            isSuper = true;
                            break;
                        }
                    }

                    Set<PsiClass> supersFrom = PSIUtil.getAllSupers(moveFromClass, allClasses);//new HashSet<PsiClass>(Arrays.asList(moveFromClass.getSupers()));
                    for (PsiClass sup : supersFrom) {
                        if (sup.equals(moveToClass)) {
                            isSuper = true;
                            break;
                        }
                    }
                    supersFrom.retainAll(supersTo);
                    boolean isOverride = false;

                    if (isSuper) {
                        continue;
                    }

                    for (PsiClass sup : supersFrom) {
                        PsiMethod[] methods = sup.getMethods();
                        for (PsiMethod m : methods) {
                            if (m.equals(method)) {
                                isOverride = true;
                                break;
                            }
                        }

                        if (isOverride) {
                            break;
                        }
                    }

                    if (!isOverride) {
                        refactorings.put(entity.getName(), entities.get(idClass).getClassName());
                    }
                }
            }
        }

        return refactorings;
    }

    public void printTableDistances() {
        int maxLen = 0;
        for (Entity ent : entities) {
            maxLen = Math.max(maxLen, ent.getName().length() + 4);
        }

        System.out.print(String.format("%1$" + maxLen + "s", ""));
        for (Entity ent : entities) {
            String name = String.format("%1$" + maxLen + "s", ent.getName());
            System.out.print(name);
        }
        System.out.println();

        for (Entity ent : entities) {
            String name = String.format("%1$" + maxLen + "s", ent.getName());
            System.out.print(name);
            for (Entity entity : entities) {
                double dist = ent.dist(entity);
                String d = "";
                if (dist == Double.MAX_VALUE) {
                    d = String.format("%1$" + maxLen + "s", "inf");
                } else {
                    d = String.format("  %." + (maxLen - 4) + "f", dist);
                }
                System.out.print(d);
            }
            System.out.println();
        }
    }

    /*public void ELKIrun() {
        double[][] data = new double[entities.size()][1];
        for (int i = 0; i < entities.size(); ++i) {
            data[i][0] = i;
        }

        System.out.println(data.length);
        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
        Database db = new StaticArrayDatabase(dbc, null);
        db.initialize();

        System.out.println("here 1");

        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);

        KMeansLloyd<NumberVector> km = new KMeansLloyd<NumberVector>(new EntityDistance(), 5, 20, init);
        System.out.println("here 2");
        Clustering<KMeansModel> c = km.run(db);
        System.out.println("here 3");
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        // We know that the ids must be a continuous range:
        DBIDRange ids = (DBIDRange) rel.getDBIDs();

        int i = 0;
        for(Cluster<KMeansModel> clu : c.getAllClusters()) {
            // K-means will name all clusters "Cluster" in lack of noise support:
            System.out.println("#" + i + ": " + clu.getNameAutomatic());
            System.out.println("Size: " + clu.size());
            System.out.println("Center: " + clu.getModel().getPrototype().toString());
            // Iterate over objects:
            System.out.print("Objects: ");
            for(DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                // To get the vector use:
                NumberVector v = rel.get(it);

                // Offset within our DBID range: "line number"
                final int offset = ids.getOffset(it);
                System.out.print(" " + offset);
                // Do NOT rely on using "internalGetIndex()" directly!
            }
            System.out.println();
            ++i;
        }
    }

    private class EntityDistance extends AbstractNumberVectorDistanceFunction {
        @Override
        public double distance(NumberVector v1, NumberVector v2) {
            return entities.get(v1.intValue(0)).dist(entities.get(v2.intValue(0)));
        }
    }
    */

    List<Entity> entities;
    Set<PsiClass> allClasses;
}

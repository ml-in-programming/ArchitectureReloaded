package org.ml_methods_group.algorithm;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import org.apache.log4j.Logger;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.config.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import edu.ucla.sspace.lsa.LatentSemanticAnalysis;
import edu.ucla.sspace.vector.DoubleVector;


public class ExtractClass extends Algorithm {

    private static final Logger LOGGER = Logging.getLogger(ExtractClass.class);

    private final List<MethodEntity> methodEntities = new ArrayList<>();
    private final List<MethodEntity> playerS = new ArrayList<>();
    private final List<MethodEntity> playerT = new ArrayList<>();

    public ExtractClass() {
        super("ExtractClass", false);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) throws IOException {
        List<Refactoring> refactorings = new ArrayList<>();
        methodEntities.addAll(context.getEntities().getMethods());

        LOGGER.info("There are " + methodEntities.size() + " methods in the class");

        if (methodEntities.size() < 2) {
            LOGGER.warn("There is less then 2 methods");
            return refactorings;
        }
        // to give the first methods to the players
        Counter counter = new Counter(methodEntities);
        int[] methodsToAdd = counter.findMinSim(methodEntities);
        add(methodsToAdd, refactorings);
        remove(methodsToAdd);

        while (methodEntities.size() > 0) {
            LOGGER.info("There are " + methodEntities.size() + " methods in the class");
            PayoffMatrix matrix = new PayoffMatrix(methodEntities, counter,  playerS, playerT);
            methodsToAdd = matrix.getBestEquilibrium();
            add(methodsToAdd, refactorings);
            remove(methodsToAdd);
        }

        return refactorings;
    }

    private void add(int[] toAdd, List<Refactoring> refactorings) {
        if(toAdd[0] != -1) {
            playerS.add(methodEntities.get(toAdd[0]));
            refactorings.add((new Refactoring(methodEntities.get(toAdd[0]).getName(), "Player S", 1, false)));
        }
        if(toAdd[1] != -1) {
            playerT.add(methodEntities.get(toAdd[1]));
            refactorings.add((new Refactoring(methodEntities.get(toAdd[1]).getName(), "Player T", 1, false)));
        }
    }

    private void remove(int[] toRemove ){
        Arrays.sort(toRemove);
        for (int i = toRemove.length - 1; i >= 0; i--) {
            if(toRemove[i] != -1)
                methodEntities.remove(toRemove[i]);
        }
    }



    /**
     * counts all metrics between methods like CIM, SSM and CSM, where
     * CIM is Call-based Interactions between Methods (takes into account method calls)
     * SSM is Structural Similarity between Methods (takes into account fields, shared by methods)
     * CSM is Conceptual Similarity between Methods (takes into account comments and identifiers similarity)
     */
    class Counter {
        private List<MethodEntity> clazz;
        private Map<String, Integer> methodCalls;
        private Map<String, DoubleVector> methodVectors;

        // for ArgoUML
        private final double WEIGHT_CIM = 0.2;
        private final double WEIGHT_SSM = 0.1;
        private final double WEIGHT_CSM = 0.7;

        public Counter(List<MethodEntity> clazz) throws IOException {
            this.clazz = clazz;
            initMethodCalls();
            initMethodVectors();
        }

        /**
         * counts incoming calls for each method and put it into HashMap
         */
        private void initMethodCalls() {
            methodCalls = new HashMap<>();
            for (MethodEntity method : clazz) {
                Set<String> methodsNames = method.getRelevantProperties().getMethods();
                Integer amount = methodCalls.get(method.getName());
                methodCalls.put(method.getName(), amount == null ? 0 : amount);

                for (String name : methodsNames) {
                    if (!name.equals(method.getName())) {
                        amount = methodCalls.get(name);
                        methodCalls.put(name, amount == null ? 1 : amount + 1);
                    }
                }
            }
        }


        /**
         * applies LSA to comments and identifiers of each method to get Map of vectors
         * @throws IOException
         */
        private void initMethodVectors() throws IOException {
            methodVectors = new HashMap<>();
            LatentSemanticAnalysis lsa = new LatentSemanticAnalysis(clazz.size(), true);
            for (MethodEntity method : clazz) {
                PsiMethod psiMethod = method.getPsiMethod();
                StringBuilder commentsAndIdentifiers = new StringBuilder(method.getName());
                psiMethod.accept(new JavaRecursiveElementVisitor() {
                    @Override
                    public void visitLocalVariable(PsiLocalVariable variable) {
                        super.visitLocalVariable(variable);
                        commentsAndIdentifiers.append(" ");
                        commentsAndIdentifiers.append(variable.getName());
                        commentsAndIdentifiers.append(" ");
                    }
                    @Override
                    public void visitComment(PsiComment comment) {
                        super.visitComment(comment);
                        commentsAndIdentifiers.append(" ");
                        commentsAndIdentifiers.append(comment.getText());
                        commentsAndIdentifiers.append(" ");
                    }
                });

                lsa.processDocument(new BufferedReader(new StringReader(commentsAndIdentifiers.toString())));
            }
            lsa.processSpace(System.getProperties());
            for (int i = 0; i < clazz.size(); i++) {
                methodVectors.put(clazz.get(i).getName(), lsa.getDocumentVector(i));
            }
        }

        /**
         * Function that measures cohesion and coupling between class and method, based on CIM, SSM and CSM
         * @param methodEntities class, represented as a list of methods
         * @param methodToAdd method
         * @return value of function
         */
        private double fCohCoupl(List<MethodEntity> methodEntities, MethodEntity methodToAdd) {
            double sum = 0;
            for (MethodEntity classMethod : methodEntities) {
                sum += sim(classMethod, methodToAdd);
            }
            return sum / clazz.size();
        }

        private double sim(MethodEntity method1, MethodEntity method2) {
            return WEIGHT_SSM * SSM(method1, method2) + WEIGHT_CIM * CIM(method1, method2) + WEIGHT_CSM * CSM (method1, method2);
        }

        private double CIM(MethodEntity method_i, MethodEntity method_j) {
            return Math.max(CIMhelper(method_i, method_j), CIMhelper(method_j, method_i));
        }

        private double SSM(MethodEntity method1, MethodEntity method2) {
            Set<String> fields1 = method1.getRelevantProperties().getFields();
            Set<String> fields2 = method2.getRelevantProperties().getFields();
            long sharedFields =  fields1.stream()
                    .filter(e -> fields2.contains(e)).count();
            long size = fields1.size() + fields2.size() - sharedFields;
            return (size == 0 ? 0 : (double) sharedFields / size);
        }

        private double CSM(MethodEntity method1, MethodEntity method2) {
            return cos(methodVectors.get(method1.getName()), methodVectors.get(method2.getName()));
        }



        private double cos(DoubleVector v1, DoubleVector v2) {
            assert v1.length() == v2.length();
            double scal = 0;
            for (int i = 0; i < v1.length(); i++) {
                scal += v1.get(i) * v2.get(i);
            }
            return scal / (v1.magnitude() * v2.magnitude());
        }

        private double CIMhelper(MethodEntity method_i, MethodEntity method_j) {
            Integer amount = methodCalls.get(method_j.getName());
            if (amount == null || amount == 0)
                return 0;
            return (method_i.getRelevantProperties().getMethods().contains(method_j.getName()) ? (1.0 / amount) : 0);
        }

        private int[] findMinSim(List<MethodEntity> methods) {
            int[] indexes = new int[2];
            double minSim = sim(methods.get(0), methods.get(0));
            for (int i = 0; i < methods.size(); i++) {
                for (int j = i; j < methods.size(); j++) {
                    double newSim = sim(methods.get(i), methods.get(j));
                    if (newSim < minSim) {
                        minSim = newSim;
                        indexes[0] = i;
                        indexes[1] = j;
                    }
                }
            }
            return indexes;
        }

    }


    /**
     * To build the payoff matrix and find the best move for each players based on Nash equilibrium
     */
    class PayoffMatrix {
        private final double M = 0.5;

        private double[][][] matrix;
        private List<MethodEntity> methods;
        private List<MethodEntity> playerS;
        private List<MethodEntity> playerT;
        private Counter counter;


        public PayoffMatrix(List<MethodEntity> methods, Counter counter, List<MethodEntity> playerS, List<MethodEntity> playerT) {
            this.methods = methods;
            this.counter = counter;
            this.playerS = playerS;
            this.playerT = playerT;
            matrix = new double[methods.size() + 1][methods.size() + 1][2];
            fillMatrix();
        }

        public double[][][] getMatrix() {
            return matrix;
        }


        /**
         * matrix[i][j] means situation, when player S takes method with number (i-1), player T takes method with number (j-1)
         * i == 0 means that player S doesn't take any methods (resp. with j and player T)
         * element of matrix[i][j] is a couple of payoffs (one for each player)
         */
        private void fillMatrix() {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (i == j) {
                        matrix[i][j][0] = -1;
                        matrix[i][j][1] = -1;
                    }
                    else if (i != 0 && j != 0) {
                        matrix[i][j][0] = counter.fCohCoupl(playerS, methods.get(i - 1)) - counter.fCohCoupl(playerS, methods.get(j - 1));
                        matrix[i][j][1] = counter.fCohCoupl(playerT, methods.get(j - 1)) - counter.fCohCoupl(playerT, methods.get(i - 1));

                    }
                    else if (i != 0 && j == 0) {
                        matrix[i][j][0] = counter.fCohCoupl(playerS, methods.get(i - 1));
                        matrix[i][j][1] = M - counter.fCohCoupl(playerT, methods.get(i - 1));
                    }
                    else if (j != 0 && i == 0) {
                        matrix[i][j][0] = M - counter.fCohCoupl(playerS, methods.get(j - 1));
                        matrix[i][j][1] = counter.fCohCoupl(playerT, methods.get(j - 1));
                    }
                }
            }

        }

        /**
         * find the best Nash Equilibrium with the highest total payoff
         * @return indexes of methods, which players should take to get the highest total payoff
         */
        public int[] getBestEquilibrium() {
            ArrayList<int[]> equilibrium = findNashEquilibrium();
            if (equilibrium.size() == 0) {
                LOGGER.warn("No equilibriums found");
            }
            ArrayList<int[]> bestEquilibriums = new ArrayList<>();
            int[] indexes = equilibrium.get(0);
            double higherPayoff = matrix[indexes[0]][indexes[1]][0] + matrix[indexes[0]][indexes[1]][1];
            for (int[] equilInd : equilibrium) {
                double newPayoff = matrix[equilInd[0]][equilInd[1]][0] + matrix[equilInd[0]][equilInd[1]][1];
                if (newPayoff == higherPayoff) {
                    bestEquilibriums.add(equilInd);
                }
                if (newPayoff > higherPayoff) {
                    bestEquilibriums.clear();
                    bestEquilibriums.add(equilInd);
                    higherPayoff = newPayoff;
                }
            }
            if(bestEquilibriums.size() > 1) {
                LOGGER.warn("Several best equilibriums found");
            }
            // вот тут от каждого отнять единичку видимо лан дико тупой вариант, переделаю после метрик
            bestEquilibriums.get(0)[0]--;
            bestEquilibriums.get(0)[1]--;
            return bestEquilibriums.get(0);
        }

        /**
         * quite stupid realisation of finding Nash equillibriums, but it works
         * @return list of equilibriums
         */
        private ArrayList<int[]> findNashEquilibrium() {
            ArrayList<int[]> equilibrium = new ArrayList<>();
            for (int i = 0; i < matrix.length; i++) {
                for (int j : findMax(matrix, i, 1))
                    if (isInList(i, findMax(matrix, j, 0))) {
                        equilibrium.add(new int[]{i, j});
                    }
            }
            return equilibrium;
        }

        private boolean isInList (int a, ArrayList<Integer> list) {
            for (int i : list) {
                if (i == a)
                    return true;
            }
            return false;
        }

        /**
         * returns indexes of max elements in row or column depends on direction
         * @param matrix pretty clear
         * @param j the number of row/column
         * @param dir 0 means vertical, 1 means horizontal
         * @return
         */
        private ArrayList<Integer> findMax(double [][][] matrix,  int j, int dir) {
            ArrayList<Integer> maxs = new ArrayList<>();
            maxs.add(0);
            double max = mDir(dir, 0, j, matrix);

            for (int i = 1; i < matrix.length; i++) {
                double next = mDir(dir, i, j, matrix);
                if (next == max)
                    maxs.add(i);
                if (next > max) {
                    max = next;
                    maxs.clear();
                    maxs.add(i);
                }
            }
            return maxs;
        }

        private double mDir (int dir, int i, int j, double [][][]matrix) {
            if (dir == 0) return matrix[i][j][0];
            else return matrix[j][i][1];
        }
    }
}

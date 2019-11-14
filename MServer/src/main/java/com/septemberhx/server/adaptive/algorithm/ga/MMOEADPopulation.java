package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.core.MServerOperator;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.septemberhx.server.adaptive.algorithm.ga.MGAUtils.fastNonDominatedSort;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/13
 */
public class MMOEADPopulation extends MBaseGA {

    private ExecutorService fixedThreadPool;

    public MMOEADPopulation(MServerOperator serverOperator, MServerOperator rawOperator) {
        super(serverOperator, rawOperator);
        int maxThread = Math.min(50, Runtime.getRuntime().availableProcessors());
        this.fixedThreadPool = Executors.newFixedThreadPool(maxThread);
    }

    public void init() {
        this.population = new MPopulation();
        CountDownLatch firstLatch = new CountDownLatch(Configuration.POPULATION_SIZE);
        for (int i = 0; i < Configuration.POPULATION_SIZE; ++i) {
            this.fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    population.populace.add(MChromosome.randomInit(
                            MBaseGA.fixedNodeIdList.size(),
                            MBaseGA.fixedServiceIdList.size(),
                            rawOperator,
                            10
                    ));
                    firstLatch.countDown();
                }
            });
        }

        try {
            firstLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.population.normalizeObjectValues();
    }

    @Override
    public MServerOperator evolve() {
        this.init();

        List<MChromosome> P_EP = new ArrayList<>();
        double[][] weightVectors = generateWeightVectors(Configuration.POPULATION_SIZE);
        int[][] B = getNeighbors(weightVectors, Configuration.MOEAD_NEIGHBOR_SIZE);
//        double[] z = getRefrencePoint(this.population);

        MChromosome bestOne = null;
        for (int i = 0; i < Configuration.MOEAD_MAX_ROUND; ++i) {
            logger.info("Round " + i);

            List<MChromosome> nextG = new Vector<>();  // thread-safe

            CountDownLatch firstLatch = new CountDownLatch(Configuration.POPULATION_SIZE);
            for (int j = 0; j < Configuration.POPULATION_SIZE; ++j) {
                int finalJ = j;
                this.fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MChromosome father1 = population.getPopulace().get(B[finalJ][MGAUtils.CROSSOVER_PROB_RAND.nextInt(Configuration.MOEAD_NEIGHBOR_SIZE)]);
                            MChromosome father2 = population.getPopulace().get(B[finalJ][MGAUtils.CROSSOVER_PROB_RAND.nextInt(Configuration.MOEAD_NEIGHBOR_SIZE)]);
                            List<MChromosome> children = father1.crossover(father2);
                            if (MGAUtils.MUTATION_PROB_RAND.nextDouble() < Configuration.MOEAD_MUTATION_RATE) {
                                children.forEach(MChromosome::mutation);
                            }
                            children.forEach(MChromosome::afterBorn);

                            MChromosome bestChild;
                            if (MGAUtils.dominates(children.get(0), children.get(1)) == MGAUtils.DOMINANT) {
                                bestChild = children.get(0);
                            } else {
                                bestChild = children.get(1);
                            }

                            for (int k = 0; k < Configuration.MOEAD_NEIGHBOR_SIZE; ++k) {
                                if (MGAUtils.dominates(bestChild, population.getPopulace().get(B[finalJ][k])) == MGAUtils.DOMINANT) {
                                    population.getPopulace().set(B[finalJ][k], bestChild);
                                }
                            }
                            nextG.add(bestChild);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            firstLatch.countDown();
                        }
                    }
                });
            }
            try {
                firstLatch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }

            nextG.addAll(P_EP);
            MPopulation.normalizeObjectValues(nextG);

            Map<Integer, List<MChromosome>> paretoFront = fastNonDominatedSort(nextG);
            P_EP = paretoFront.get(1);

            if (bestOne == null) {
                bestOne = Collections.min(P_EP, Comparator.comparingDouble(MChromosome::getNormWSGAFitness));
            } else {
                MChromosome currBest = Collections.min(P_EP, Comparator.comparingDouble(MChromosome::getNormWSGAFitness));
                if (bestOne.getNormWSGAFitness() > currBest.getNormWSGAFitness()) {
                    bestOne = currBest;
                }
            }

            logger.info("Best: " + bestOne.getNormWSGAFitness());
            logger.info(bestOne.getFitness());
            logger.info(bestOne.getCost());
            bestOne.getCurrOperator().printStatus();

            if (i % 10 == 0) {
                System.gc();
            }
        }

        // the result: P_EP
        bestOne.getCurrOperator().printStatus();
        logger.info(bestOne.getNormObjectiveValues());

        this.fixedThreadPool.shutdown();
        return bestOne.getCurrOperator();
    }

    private static double[] getRefrencePoint(MPopulation population) {
        double[] z = new double[2];
        z[0] = population.getPopulace().get(0).getNormObjectiveValues().get(0);
        z[1] = population.getPopulace().get(0).getNormObjectiveValues().get(1);
        for (int i = 1; i < population.getPopulace().size(); ++i) {
            if (population.getPopulace().get(i).getNormObjectiveValues().get(0) < z[0]) {
                z[0] = population.getPopulace().get(i).getNormObjectiveValues().get(0);
            }

            if (population.getPopulace().get(i).getNormObjectiveValues().get(1) < z[1]) {
                z[1] = population.getPopulace().get(i).getNormObjectiveValues().get(1);
            }
        }
        return z;
    }

    private static double[][] generateWeightVectors(int N) {
        // fixed objective size to 2
        double[][] weightVectors = new double[N][2];

        double slice = 1.0 / (N - 1);
        for (int i = 0; i < N; i++) {
            weightVectors[i][0] = i * slice;
            weightVectors[i][1] = (1 - i * slice);
        }

        return weightVectors;
    }

    private static int[][] getNeighbors(double[][] weightVectors, int neighborSize) {
        int N = weightVectors.length;
        double[][] distances = getDistances(weightVectors);

        int[][] neighbors = new int[N][neighborSize];
        for (int i = 0; i < N; i++) {
            neighbors[i] = getSmallestValues(distances[i], neighborSize);
        }
        return neighbors;
    }

    private static double[][] getDistances(double[][] weightVectors) {
        int N = weightVectors.length;
        double[][] distances = new double[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                double distance = Math
                        .sqrt(Math.pow(weightVectors[i][0]
                                - weightVectors[j][0], 2)
                                + Math.pow(weightVectors[i][1]
                                - weightVectors[j][1], 2));
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }
        return distances;
    }

    private static int[] getSmallestValues(double[] distances, int neighborSize) {
        List<NeighborHelper> sortingHelper = new ArrayList<NeighborHelper>();
        for (int i = 0; i < distances.length; i++) {
            NeighborHelper n = new NeighborHelper(distances[i], i);
            sortingHelper.add(n);
        }
        Collections.sort(sortingHelper);
        int[] neighbors = new int[neighborSize];
        for (int i = 0; i < neighborSize; i++) {
            neighbors[i] = sortingHelper.get(i).index;
        }
        return neighbors;
    }

    private static class NeighborHelper implements Comparable<NeighborHelper>{
        double distance;
        int index;
        public NeighborHelper(double distance, int index) {
            this.distance = distance;
            this.index = index;
        }
        @Override
        public int compareTo(NeighborHelper o) {
            double diff = this.distance - o.distance;
            return diff == 0 ? 0 : (diff < 0 ? -1 : 1);
        }
    }
}

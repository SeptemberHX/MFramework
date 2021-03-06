package com.septemberhx.server.adaptive.algorithm.ga;
import com.septemberhx.server.core.MServerOperator;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.septemberhx.server.adaptive.algorithm.ga.MGAUtils.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/13
 */
public class MNSGAIIPopulation extends MBaseGA {

    private ExecutorService fixedThreadPool;

    public MNSGAIIPopulation(MServerOperator snapshotOperator, MServerOperator rawOperator) {
        super(snapshotOperator, rawOperator);
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

        int currRound = 1;
        Map<Integer, List<MChromosome>> paretoFront = fastNonDominatedSort(this.population.getPopulace());
        for (int i = 1; i < paretoFront.size(); ++i) {
            crowdingDistanceAssignment(paretoFront.get(i));
        }

        MChromosome bestOne = null;
        while (currRound <= Configuration.NSGAII_MAX_ROUND) {
            logger.info("Round " + currRound);

            List<MChromosome> nextG = new Vector<>();  // thread-safe
            CountDownLatch firstLatch = new CountDownLatch(Configuration.POPULATION_SIZE);
            for (int i = 0; i < Configuration.POPULATION_SIZE; ++i) {
                this.fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MChromosome parent1 = binaryTournamentSelection(population);
                            MChromosome parent2 = binaryTournamentSelection(population);

                            if (Configuration.DEBUG_MODE) {
                                if (!parent1.verify()) {
                                    logger.error(parent1.getId() + " failed to verify before crossover");
                                }
                                if (!parent2.verify()) {
                                    logger.error(parent2.getId() + " failed to verify before crossover");
                                }
                            }

                            List<MChromosome> children = parent1.crossover(parent2);
                            if (MGAUtils.MUTATION_PROB_RAND.nextDouble() < Configuration.NSGAII_MUTATION_RATE) {
                                children.forEach(MChromosome::mutation);
                            }
                            children.forEach(MChromosome::afterBorn);

                            if (Configuration.DEBUG_MODE) {
                                if (!parent1.verify()) {
                                    logger.error(parent1.getId() + " failed to verify after crossover");
                                }
                                if (!parent2.verify()) {
                                    logger.error(parent2.getId() + " failed to verify after crossover");
                                }
                            }
                            nextG.addAll(children);
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

            nextG.addAll(this.population.getPopulace());
            MPopulation.normalizeObjectValues(nextG);

            paretoFront = fastNonDominatedSort(nextG);
            for (int i = 1; i < paretoFront.size(); ++i) {  // the pareto level starts from 1
                crowdingDistanceAssignment(paretoFront.get(i));
            }
            order(nextG);
            this.population.setPopulace(nextG.subList(0, Configuration.POPULATION_SIZE));
            ++currRound;

            if (bestOne == null) {
                bestOne = Collections.min(this.population.getPopulace(), Comparator.comparingDouble(MChromosome::getNormWSGAFitness));
            } else {
                MChromosome currBest = Collections.min(this.population.getPopulace(), Comparator.comparingDouble(MChromosome::getNormWSGAFitness));
                if (bestOne.getNormWSGAFitness() > currBest.getNormWSGAFitness()) {
                    bestOne = currBest;
                }
            }

            logger.info("Best: " + bestOne.getNormWSGAFitness());
            logger.info(bestOne.getFitness());
            logger.info(bestOne.getCost());
//            bestOne.getCurrOperator().printStatus();

            if (currRound % 10 == 0) {
                System.gc();
            }
        }
        bestOne.getCurrOperator().printStatus();

        this.fixedThreadPool.shutdown();
        return bestOne.getCurrOperator();
    }

    private static void order(List<MChromosome> chromosomeList) {
        chromosomeList.sort((o1, o2) -> {
            if (o1.getRank() != o2.getRank()) {
                return Integer.compare(o1.getRank(), o2.getRank());
            } else {
                return -Double.compare(o1.getCrowdingDistance(), o2.getCrowdingDistance());
            }
        });
    }

    private static MChromosome binaryTournamentSelection(MPopulation population) {
        MChromosome individual1 = population.getPopulace().get(MGAUtils.CROSSOVER_PROB_RAND.nextInt(population.getPopulace().size()));
        MChromosome individual2 = population.getPopulace().get(MGAUtils.CROSSOVER_PROB_RAND.nextInt(population.getPopulace().size()));

        if (individual1.getRank() > individual2.getRank()) return individual1;
        else if (individual1.getRank() == individual2.getRank()) {
            if (individual1.getCrowdingDistance() > individual2.getCrowdingDistance()) return individual1;
            else return individual2;
        } else {
            return individual2;
        }
    }

    private static void crowdingDistanceAssignment(final List<MChromosome> nondominatedChromosomes) {
        int size = nondominatedChromosomes.size();
        for(int i = 0; i < 2; i++) {
            sortAgainstObjective(nondominatedChromosomes, i);
            nondominatedChromosomes.get(0).setCrowdingDistance(Double.MAX_VALUE);
            nondominatedChromosomes.get(size - 1).setCrowdingDistance(Double.MAX_VALUE);
            double maxObjectiveValue = nondominatedChromosomes.get(0).getNormObjectiveValues().get(i);
            double minObjectiveValue = nondominatedChromosomes.get(size - 1).getNormObjectiveValues().get(i);
            for(int j = 1; j < size - 1; j++) {
                if (nondominatedChromosomes.get(j).getCrowdingDistance() < Double.MAX_VALUE) {
                    nondominatedChromosomes.get(j).setCrowdingDistance(
                            nondominatedChromosomes.get(j).getCrowdingDistance() + (
                                    (nondominatedChromosomes.get(j + 1).getNormObjectiveValues().get(i) - nondominatedChromosomes.get(j - 1).getNormObjectiveValues().get(i)) / (maxObjectiveValue - minObjectiveValue)
                            )
                    );
                }
            }
        }
    }

    private static void sortAgainstObjective(final List<MChromosome> chromosomes, int objectiveIndex) {
        chromosomes.sort((o1, o2) ->
                -o1.getNormObjectiveValues().get(objectiveIndex).compareTo(o2.getNormObjectiveValues().get(objectiveIndex))
        );
    }
}

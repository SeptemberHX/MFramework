package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/4
 */
public class MWSGAPopulation extends MBaseGA {

    public static Double W_SCORE = 0.5;
    public static Double W_COST = 0.5;

    private ExecutorService fixedThreadPool;

    public MWSGAPopulation(MServerOperator snapshotOperator, MServerOperator rawOperator) {
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
        while (currRound <= Configuration.WSGA_MAX_ROUND) {
            logger.info("Round " + currRound);

            List<MChromosome> nextG = new Vector<>();  // thread-safe
            CountDownLatch firstLatch = new CountDownLatch(Configuration.POPULATION_SIZE / 2);
            for (int i = 0; i < Configuration.POPULATION_SIZE / 2; ++i) {
                List<MChromosome> finalNextG = nextG;
                this.fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MChromosome parent1 = binaryTournamentSelection(population);
                            MChromosome parent2 = binaryTournamentSelection(population);
                            List<MChromosome> children = parent1.crossover(parent2);
                            if (MGAUtils.MUTATION_PROB_RAND.nextDouble() < Configuration.WSGA_MUTATION_RATE) {
                                children.forEach(MChromosome::mutation);
                            }
                            children.forEach(MChromosome::afterBorn);
                            finalNextG.addAll(children);
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

            nextG.sort(Comparator.comparingDouble(MChromosome::getNormWSGAFitness));
            nextG = nextG.subList(0, Configuration.POPULATION_SIZE);
            this.population.setPopulace(nextG);
            ++currRound;

            logger.info("Best: " + this.population.getPopulace().get(0).getNormWSGAFitness());
            logger.info(this.population.getPopulace().get(0).getFitness());
            logger.info(this.population.getPopulace().get(0).getCost());
            this.population.getPopulace().get(0).getCurrOperator().printStatus();

            if (currRound % 10 == 0) {
                System.gc();
            }
        }

        this.population.getPopulace().get(0).getCurrOperator().printStatus();
        this.fixedThreadPool.shutdown();
        return this.population.getPopulace().get(0).getCurrOperator();
    }

    private static MChromosome binaryTournamentSelection(MPopulation population) {
        MChromosome individual1 = population.getPopulace().get(MGAUtils.CROSSOVER_PROB_RAND.nextInt(population.getPopulace().size()));
        MChromosome individual2 = population.getPopulace().get(MGAUtils.CROSSOVER_PROB_RAND.nextInt(population.getPopulace().size()));

        if (individual1.getFitness() > individual2.getFitness()) return individual1; else return individual2;
    }
}

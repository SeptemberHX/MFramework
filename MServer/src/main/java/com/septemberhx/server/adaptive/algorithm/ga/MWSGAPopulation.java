package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/4
 */
public class MWSGAPopulation extends MBaseGA {

    public static Double W_SCORE = 1.0;
    public static Double W_COST = 1.0;
    public static Double P_SCORE = 0.5;
    public static Double P_COST = 0.5;

    public MWSGAPopulation(MServerOperator snapshotOperator) {
        super(snapshotOperator);
    }

    public void init() {
        this.population = new MPopulation();
        for (int i = 0; i < Configuration.POPULATION_SIZE; ++i) {
            this.population.populace.add(MChromosome.randomInit(
                    MBaseGA.fixedNodeIdList.size(),
                    MBaseGA.fixedServiceIdList.size(),
                    MSystemModel.getIns().getOperator(),
                    10
            ));
        }
    }

    @Override
    public void evolve() {
        this.init();

        int currRound = 1;
        this.population.calcWSGAFitness();
        while (currRound <= Configuration.WSGA_MAX_ROUND) {
            logger.info("Round " + currRound);

            List<MChromosome> nextG = new ArrayList<>();
            while (nextG.size() < Configuration.POPULATION_SIZE) {
                MChromosome parent1 = binaryTournamentSelection(this.population);
                MChromosome parent2 = binaryTournamentSelection(this.population);
                List<MChromosome> children = parent1.crossover(parent2);
                if (MGAUtils.MUTATION_PROB_RAND.nextDouble() < Configuration.WSGA_MUTATION_RATE) {
                    children.forEach(MChromosome::mutation);
                }
                children.forEach(MChromosome::afterBorn);
                nextG.addAll(children);
            }

            nextG.addAll(this.population.getPopulace());
            nextG.sort((o1, o2) -> -Double.compare(o1.getWSGAFitness(), o2.getWSGAFitness()));
            nextG = nextG.subList(0, Configuration.POPULATION_SIZE);
            this.population.setPopulace(nextG);
            ++currRound;

            logger.info("Best: " + this.population.getPopulace().get(0).getWSGAFitness());
        }

        this.population.getPopulace().get(0).getCurrOperator().printStatus();
    }

    private static MChromosome binaryTournamentSelection(MPopulation population) {
        MChromosome individual1 = population.getPopulace().get(MGAUtils.CROSSOVER_PROB_RAND.nextInt(population.getPopulace().size()));
        MChromosome individual2 = population.getPopulace().get(MGAUtils.CROSSOVER_PROB_RAND.nextInt(population.getPopulace().size()));

        if (individual1.getFitness() > individual2.getFitness()) return individual1; else return individual2;
    }
}

package com.septemberhx.server.adaptive.algorithm.ga;
import com.septemberhx.server.core.MServerOperator;

import java.util.*;

import static com.septemberhx.server.adaptive.algorithm.ga.MGAUtils.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/13
 */
public class MNSGAIIPopulation extends MBaseGA {

    public MNSGAIIPopulation(MServerOperator snapshotOperator) {
        super(snapshotOperator);
    }

    public void init(int populationSize, int maxRound) {

    }

    @Override
    public void evolve() {
        int currRound = 1;
        this.population.calcNSGAIIFitness();
        Map<Integer, List<MChromosome>> paretoFront = fastNonDominatedSort(this.population.getPopulace());
        for (int i = 1; i < paretoFront.size(); ++i) {
            crowdingDistanceAssignment(paretoFront.get(i));
        }

        while (currRound <= Configuration.NSGAII_MAX_ROUND) {
            List<MChromosome> nextG = new ArrayList<>();
            for (int i = 0; i < Configuration.POPULATION_SIZE; ++i) {
                MChromosome parent1 = binaryTournamentSelection(this.population);
                MChromosome parent2 = binaryTournamentSelection(this.population);
                List<MChromosome> children = parent1.crossover(parent2);
                if (MGAUtils.MUTATION_PROB_RAND.nextDouble() < Configuration.NSGAII_MUTATION_RATE) {
                    children.forEach(c -> {
                        c.mutation();
                        c.afterBorn();
                    });
                }
                nextG.addAll(children);
            }

            for (MChromosome child : nextG) {
                child.calcNSGAIIFitness();
            }
            nextG.addAll(this.population.getPopulace());
            paretoFront = fastNonDominatedSort(nextG);
            for (int i = 0; i < paretoFront.size(); ++i) {
                crowdingDistanceAssignment(paretoFront.get(i));
            }
            order(nextG);
            this.population.setPopulace(nextG.subList(0, Configuration.POPULATION_SIZE));
        }
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
            double maxObjectiveValue = nondominatedChromosomes.get(0).getObjectiveValues().get(i);
            double minObjectiveValue = nondominatedChromosomes.get(size - 1).getObjectiveValues().get(i);
            for(int j = 1; j < size - 1; j++) {
                if (nondominatedChromosomes.get(j).getCrowdingDistance() < Double.MAX_VALUE) {
                    nondominatedChromosomes.get(j).setCrowdingDistance(
                            nondominatedChromosomes.get(j).getCrowdingDistance() + (
                                    (nondominatedChromosomes.get(j + 1).getObjectiveValues().get(i) - nondominatedChromosomes.get(j - 1).getObjectiveValues().get(i)) / (maxObjectiveValue - minObjectiveValue)
                            )
                    );
                }
            }
        }
    }

    private static void sortAgainstObjective(final List<MChromosome> chromosomes, int objectiveIndex) {
        chromosomes.sort((o1, o2) ->
                -o1.getObjectiveValues().get(objectiveIndex).compareTo(o2.getObjectiveValues().get(objectiveIndex))
        );
    }
}

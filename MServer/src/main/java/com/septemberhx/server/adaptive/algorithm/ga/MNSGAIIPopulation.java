package com.septemberhx.server.adaptive.algorithm.ga;
import com.septemberhx.server.core.MServerOperator;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/13
 */
public class MNSGAIIPopulation extends MBaseGA {
    private static final int DOMINANT = 1;
    private static final int INFERIOR = 2;
    private static final int NON_DOMINATED = 3;
    private static Random LOCAL_RANDOM = new Random(100000000);

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

        while (currRound <= maxRound) {
            List<MChromosome> nextG = new ArrayList<>();
            for (int i = 0; i < Configuration.POPULATION_SIZE; ++i) {
                MChromosome parent1 = binaryTournamentSelection(this.population);
                MChromosome parent2 = binaryTournamentSelection(this.population);
                List<MChromosome> children = parent1.crossover(parent2);
                if (MGAUtils.MUTATION_PROB_RAND.nextDouble() < this.mutationRate) {
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

    /**
     * Calculate the domination rank of each chromosome
     * Sort dominated chromosomes against crowding distance at each rank and return the result map
     * @param populace all chromosomes
     * @return Sorted map
     */
    private static Map<Integer, List<MChromosome>>  fastNonDominatedSort(final List<MChromosome> populace) {
        for (MChromosome chromosome : populace) {
            chromosome.reset();
        }
        for (int i = 0; i < populace.size() - 1; i++) {
            for (int j = i + 1; j < populace.size() - 1; j++) {
                switch (dominates(populace.get(i), populace.get(j))) {
                    case DOMINANT:
                        populace.get(i).setDominatedChromosome(populace.get(j));
                        populace.get(j).incrementDominationCount(1);
                        break;
                    case INFERIOR:
                        populace.get(i).incrementDominationCount(1);
                        populace.get(j).setDominatedChromosome(populace.get(i));
                        break;
                    case NON_DOMINATED: break;
                }
            }
            if (populace.get(i).getDominationCount() == 0) populace.get(i).setRank(1);
        }

        Map<Integer, List<MChromosome>> paretoFront = new HashMap<>();
        List<MChromosome> leftList = populace;
        int miniumCount;
        int currRank = 1;
        while ((miniumCount = getMiniumDominationCount(leftList)) != -1) {
            List<MChromosome> targetList = new ArrayList<>();
            List<MChromosome> nextList = new ArrayList<>();
            for (MChromosome chromosome : leftList) {
                if (chromosome.getDominationCount() == miniumCount) {
                    chromosome.setRank(currRank);
                    targetList.add(chromosome);
                } else {
                    nextList.add(chromosome);
                }
            }

            paretoFront.put(currRank, targetList);
            for (MChromosome target : targetList) {
                for (MChromosome chromosome : target.getDominatedChromosomes()) {
                    chromosome.incrementDominationCount(-1);
                }
            }

            leftList = nextList;
            ++currRank;
        }

        if (!leftList.isEmpty()) {
            throw new RuntimeException("Your fastNonDominatedSort get wrong result!!!");
        }
        return paretoFront;
    }

    private static Integer getMiniumDominationCount(List<MChromosome> chromosomeList) {
        int miniumCount = -1;
        for (MChromosome chromosome : chromosomeList) {
            if (miniumCount == -1 || miniumCount > chromosome.getDominationCount()) {
                miniumCount = chromosome.getDominationCount();
            }
        }
        return miniumCount;
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

    private static int dominates(final MChromosome chromosome1, final MChromosome chromosome2) {
        if(isDominant(chromosome1, chromosome2)) return DOMINANT;
        else if(isDominant(chromosome2, chromosome1)) return INFERIOR;
        else return NON_DOMINATED;
    }

    private static boolean isDominant(final MChromosome chromosome1, final MChromosome chromosome2) {
        boolean isDominant = true;
        boolean atLeastOneIsLarger = false;
        for(int i = 0; i < 2; i++) {
            if(chromosome1.getObjectiveValues().get(i) < chromosome2.getObjectiveValues().get(i)) {
                isDominant = false;
                break;
            } else if (!atLeastOneIsLarger && (chromosome1.getObjectiveValues().get(i) > chromosome2.getObjectiveValues().get(i))) {
                atLeastOneIsLarger = true;
            }
        }
        return isDominant && atLeastOneIsLarger;
    }
}

package com.septemberhx.server.adaptive.algorithm.ga;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/14
 */
public class MGAUtils {
    public static Random CROSSOVER_POINT_RAND = new Random(1000000);
    public static Random CROSSOVER_PROB_RAND = new Random(3000000);
    public static Random MUTATION_PROB_RAND = new Random(4000000);
    public static Random MUTATION_SELECT_RAND = new Random(5000000);

    static final int DOMINANT = 1;
    static final int INFERIOR = 2;
    static final int NON_DOMINATED = 3;

    private static boolean isDominant(final MChromosome chromosome1, final MChromosome chromosome2) {
        boolean isDominant = true;
        boolean atLeastOneIsLarger = false;
        for(int i = 0; i < 2; i++) {
            if(chromosome1.getObjectiveValues().get(i) > chromosome2.getObjectiveValues().get(i)) {
                isDominant = false;
                break;
            } else if (!atLeastOneIsLarger && (chromosome1.getObjectiveValues().get(i) < chromosome2.getObjectiveValues().get(i))) {
                atLeastOneIsLarger = true;
            }
        }
        return isDominant && atLeastOneIsLarger;
    }

    static int dominates(final MChromosome chromosome1, final MChromosome chromosome2) {
        if(isDominant(chromosome1, chromosome2)) return DOMINANT;
        else if(isDominant(chromosome2, chromosome1)) return INFERIOR;
        else return NON_DOMINATED;
    }


    /**
     * Calculate the domination rank of each chromosome
     * Sort dominated chromosomes against crowding distance at each rank and return the result map
     * @param populace all chromosomes
     * @return Sorted map
     */
    static Map<Integer, List<MChromosome>> fastNonDominatedSort(final List<MChromosome> populace) {
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
}

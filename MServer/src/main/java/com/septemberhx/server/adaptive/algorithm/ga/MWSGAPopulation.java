package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import org.javatuples.Pair;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/4
 */
public class MWSGAPopulation {

    private List<MChromosome> chromosomes = new ArrayList<>();

    public static Random CROSSOVER_POINT_RAND = new Random(1000000);
    public static Random CROSSOVER_PROB_RAND = new Random(3000000);
    public static Random MUTATION_PROB_RAND = new Random(4000000);
    public static Random MUTATION_SELECT_RAND = new Random(5000000);

    public static Double W_SCORE = 1.0;
    public static Double W_COST = 1.0;
    public static Double P_SCORE = 0.5;
    public static Double P_COST = 0.5;

    public static List<String> fixedNodeIdList;
    public static List<String> fixedServiceIdList;
    public static Map<String, Integer> fixedNodeId2Index;
    public static Map<String, Integer> fixedServiceId2Index;

    private int populationSize = 100;
    private int maxRound = 200;
    private double mutationRate = 0.25;

    public MWSGAPopulation(MServerOperator snapshotOperator) {
        fixedNodeIdList = new ArrayList<>();
        fixedServiceIdList = new ArrayList<>();
        MSystemModel.getIns().getMSNManager().getFixedOrderNodeList().forEach(n -> fixedNodeIdList.add(n.getId()));
        snapshotOperator.getServiceManager().getFixedServiceList().forEach(s -> fixedServiceIdList.add(s.getId()));

        fixedNodeId2Index = new HashMap<>();
        for (int i = 0; i < fixedNodeIdList.size(); ++i) {
            fixedNodeId2Index.put(fixedNodeIdList.get(i), i);
        }

        fixedServiceId2Index = new HashMap<>();
        for (int i = 0; i < fixedServiceIdList.size(); ++i) {
            fixedServiceId2Index.put(fixedServiceIdList.get(i), i);
        }
    }

    public void init(int populationSize, int maxRound) {

    }

    public void evolve() {
        int currRound = 1;
        while (currRound <= maxRound) {
            this.chromosomes.sort(new Comparator<MChromosome>() {
                @Override
                public int compare(MChromosome o1, MChromosome o2) {
                    return -Double.compare(o1.getWSGAFitness(), o2.getWSGAFitness());
                }
            });
            this.chromosomes = this.chromosomes.subList(0, this.populationSize);    // eliminate
            List<MChromosome> nextG = new ArrayList<>();

            while (nextG.size() < this.populationSize) {
                Pair<Integer, Integer> parentIndics = this.randomParentIndics();
                List<MChromosome> children = this.chromosomes.get(parentIndics.getValue0()).crossover(this.chromosomes.get(parentIndics.getValue1()));
                if (MUTATION_PROB_RAND.nextDouble() < this.mutationRate) {
                    children.forEach(c -> {
                        c.mutation();
                        c.afterBorn();
                    });
                }
                nextG.addAll(children);
            }
        }
    }

    private Pair<Integer, Integer> randomParentIndics() {
        int p1 = CROSSOVER_PROB_RAND.nextInt(this.populationSize);
        int p2;
        do {
            p2 = CROSSOVER_PROB_RAND.nextInt(this.populationSize);
        } while (p2 == p1);
        return new Pair<>(p1, p2);
    }
}

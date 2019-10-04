package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.core.MServerOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/4
 */
public class MChromosome {

    private MGene[] genes;
    private int geneLength;
    private MServerOperator rawOperator;

    public MChromosome(int nodeSize, int serviceSize, MServerOperator rawOperator) {
        this.genes = new MGene[nodeSize];
        this.geneLength = serviceSize;
        for (int i = 0; i < nodeSize; ++i) {
            this.genes[i] = new MGene(serviceSize);
        }
        this.rawOperator = rawOperator;
    }

    public MChromosome(MGene[] genes, int geneLength, MServerOperator rawOperator) {
        this.genes = genes;
        this.geneLength = geneLength;
        this.rawOperator = rawOperator;
    }

    public List<MChromosome> crossover(MChromosome other) {
        // two point crossover
        int index1 = MPopulation.CROSSOVER_POINT_RAND.nextInt(this.geneLength);
        int index2 = MPopulation.CROSSOVER_POINT_RAND.nextInt(this.geneLength);

        int startIndex = Math.min(index1, index2);
        int endIndex = Math.max(index1, index2);

        MGene[] childGene1 = new MGene[this.genes.length];
        MGene[] childGene2 = new MGene[this.genes.length];
        for (int i = 0; i < genes.length; ++i) {
            ArrayList<MGene> childGenes = this.genes[i].crossoverTwoPoint(other.genes[i], startIndex, endIndex);
            assert childGenes.size() == 2;
            childGene1[i] = childGenes.get(0);
            childGene2[i] = childGenes.get(1);
        }

        List<MChromosome> resultList = new ArrayList<>();
        resultList.add(new MChromosome(childGene1, this.geneLength, this.rawOperator));
        resultList.add(new MChromosome(childGene2, this.geneLength, this.rawOperator));
        return resultList;
    }

    // todo: select instance by the frequency
    public void mutation() {
        double mutationType = MPopulation.MUTATION_SELECT_RAND.nextDouble();
        if (mutationType < 0.33) {
            this.addOneInstance();
        } else if (mutationType < 0.66) {
            this.deleteOneInstance();
        } else {
            this.moveOneInstance();
        }
    }

    private void deleteOneInstance() {
        int nodeIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.genes.length);
        List<Integer> indicsWithInstance = new ArrayList<>();
        for (int i = 0; i < this.geneLength; ++i) {
            if (this.genes[nodeIndex].getGeneIntArr()[i] > 0) {
                indicsWithInstance.add(i);
            }
        }
        int serviceIndex = indicsWithInstance.get(MPopulation.MUTATION_SELECT_RAND.nextInt(indicsWithInstance.size()));
        this.genes[nodeIndex].deleteInstance(serviceIndex);
    }

    private void addOneInstance() {
        int nodeIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.genes.length);
        int serviceIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.geneLength);
        this.genes[nodeIndex].addInstance(serviceIndex);
    }

    private void moveOneInstance() {
        int fromNodeIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.genes.length);
        int toNodeIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.genes.length);

        List<Integer> indicsWithInstance = new ArrayList<>();
        for (int i = 0; i < this.geneLength; ++i) {
            if (this.genes[fromNodeIndex].getGeneIntArr()[i] > 0) {
                indicsWithInstance.add(i);
            }
        }
        int serviceId = indicsWithInstance.get(MPopulation.MUTATION_SELECT_RAND.nextInt(indicsWithInstance.size()));
        this.genes[fromNodeIndex].deleteInstance(serviceId);
        this.genes[toNodeIndex].addInstance(serviceId);
    }

    /**
     * In this function, we will assign demands to instances, and "save" non-feasible solution
     *
     * todo: finish the afterInstancePlacement function. Use algorithms in Minor algorithm for all user demands.
     */
    private void afterInstancePlacement() {

    }
}

package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.adaptive.algorithm.MDemandAssignHA;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MUserDemand;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;

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
    private MServerOperator currOperator;
    private boolean ifDemandsAssigned = false;

    public MChromosome(int nodeSize, int serviceSize, MServerOperator rawOperator) {
        this.genes = new MGene[nodeSize];
        this.geneLength = serviceSize;
        for (int i = 0; i < nodeSize; ++i) {
            this.genes[i] = new MGene(serviceSize);
        }
        this.rawOperator = rawOperator;
        this.currOperator = rawOperator.shallowClone();
    }

    public MChromosome(MGene[] genes, int geneLength, MServerOperator rawOperator) {
        this.genes = genes;
        this.geneLength = geneLength;
        this.rawOperator = rawOperator;
        this.currOperator = rawOperator.shallowClone();
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
        MChromosome child1 = new MChromosome(childGene1, this.geneLength, this.rawOperator);
        MChromosome child2 = new MChromosome(childGene2, this.geneLength, this.rawOperator);
        resultList.add(child1);
        resultList.add(child2);
        return resultList;
    }

    // todo: select instance by the frequency
    public void mutation() {
        double mutationType = MPopulation.MUTATION_SELECT_RAND.nextDouble();
        if (mutationType < 0.33) {              // add a new instance
            this.addOneInstance();
            this.assignDemands();
        } else if (mutationType < 0.66) {       // delete an exist instance
            this.assignDemands();                   // should assign demands before deleting
            this.deleteOneInstance();
        } else {                                // move an instance to other node
            this.assignDemands();                   // and so on
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

        // re-assign user demands on this instance
        String serviceId = this.currOperator.getAllServices().get(serviceIndex).getId();
        List<MServiceInstance> instanceList = this.currOperator.getInstancesOfService(serviceId);
        int instanceIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(instanceList.size());
        String targetInstanceId = instanceList.get(instanceIndex).getId();
        List<MUserDemand> userDemands = this.currOperator.deleteInstance(targetInstanceId);
        MDemandAssignHA.calc(userDemands, this.currOperator);
    }

    private void addOneInstance() {
        int nodeIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.genes.length);
        int serviceIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.geneLength);
        this.genes[nodeIndex].addInstance(serviceIndex);
    }

    public void afterBorn() {
        this.assignDemands();
    }

    private void moveOneInstance() {
        String instanceId, targetNodeId;
        do {
            int fromNodeIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.genes.length);
            int toNodeIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(this.genes.length);

            List<Integer> indicsWithInstance = new ArrayList<>();
            for (int i = 0; i < this.geneLength; ++i) {
                if (this.genes[fromNodeIndex].getGeneIntArr()[i] > 0) {
                    indicsWithInstance.add(i);
                }
            }
            int serviceIndex = indicsWithInstance.get(MPopulation.MUTATION_SELECT_RAND.nextInt(indicsWithInstance.size()));
            this.genes[fromNodeIndex].deleteInstance(serviceIndex);
            this.genes[toNodeIndex].addInstance(serviceIndex);

            // re-assign user demands on this instance
            String serviceId = this.currOperator.getAllServices().get(serviceIndex).getId();
            List<MServiceInstance> instanceList = this.currOperator.getInstancesOfService(serviceId);
            int instanceIndex = MPopulation.MUTATION_SELECT_RAND.nextInt(instanceList.size());
            instanceId = instanceList.get(instanceIndex).getId();
            targetNodeId = MSystemModel.getIns().getMSNManager().getFixedOrderNodeList().get(toNodeIndex).getId();
        } while (!this.currOperator.moveInstance(instanceId, targetNodeId));
    }

    /**
     * In this function, we will assign demands to instances, and "save" non-feasible solution
     */
    private void assignDemands() {
        List<MUserDemand> allUserDemands = MSystemModel.getIns().getUserManager().getAllUserDemands();
        MDemandAssignHA.calc(allUserDemands, currOperator);
        this.ifDemandsAssigned = true;
    }

    public double fitness() {
        return this.currOperator.calcCost();
    }
}

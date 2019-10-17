package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.adaptive.algorithm.MDemandAssignHA;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.MBaseJob;
import com.septemberhx.server.job.MDeployJob;
import com.septemberhx.server.job.MJobType;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

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
    private List<MUserDemand> unSolvedDemandList = new ArrayList<>();

    private double fitness = -1;
    private double cost = -1;

    // --------- Belows for NSGA-II algorithm
    @Getter
    @Setter
    private double crowdingDistance;

    @Getter
    @Setter
    private int dominationCount;

    @Getter
    @Setter
    private ArrayList<MChromosome> dominatedChromosomes;

    @Getter
    @Setter
    private int rank;

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
        int index1 = MGAUtils.CROSSOVER_POINT_RAND.nextInt(this.geneLength);
        int index2 = MGAUtils.CROSSOVER_POINT_RAND.nextInt(this.geneLength);

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

        // for the crossover on each node, the demand-instance mapping should be considered carefully
        // The exist mapping should be kept if possible, or mark as need-to-solved
        List<MChromosome> resultList = new ArrayList<>();
        MChromosome child1 = new MChromosome(childGene1, this.geneLength, this.rawOperator);
        child1.currOperator = this.currOperator.shallowClone();
        child1.initGenes(this, other, startIndex, endIndex);

        MChromosome child2 = new MChromosome(childGene2, this.geneLength, this.rawOperator);
        child2.currOperator = other.currOperator.shallowClone();
        child2.initGenes(other, this, startIndex, endIndex);
        resultList.add(child1);
        resultList.add(child2);
        return resultList;
    }

    public void mutation() {
        double mutationType = MGAUtils.MUTATION_SELECT_RAND.nextDouble();
        if (mutationType < 0.33) {              // add a new instance
            this.addOneInstance();
        } else if (mutationType < 0.66) {       // delete an exist instance
            this.deleteOneInstance();
        } else {                                // move an instance to other node
            this.moveOneInstance();
        }
    }

    private void deleteOneInstance() {
        int nodeIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(this.genes.length);
        List<Integer> indicsWithInstance = new ArrayList<>();
        for (int i = 0; i < this.geneLength; ++i) {
            if (this.genes[nodeIndex].getGeneIntArr()[i] > 0) {
                indicsWithInstance.add(i);
            }
        }
        int serviceIndex = indicsWithInstance.get(MGAUtils.MUTATION_SELECT_RAND.nextInt(indicsWithInstance.size()));
        this.genes[nodeIndex].deleteInstance(serviceIndex);

        // re-assign user demands on this instance
        String serviceId = MWSGAPopulation.fixedServiceIdList.get(serviceIndex);
        List<MServiceInstance> instanceList = this.currOperator.getInstancesOfService(serviceId);
        int instanceIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(instanceList.size());
        String targetInstanceId = instanceList.get(instanceIndex).getId();
        List<MUserDemand> userDemands = this.currOperator.deleteInstance(targetInstanceId);
        MDemandAssignHA.calc(userDemands, this.currOperator);
    }

    private void addOneInstance() {
        int nodeIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(this.genes.length);
        int serviceIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(this.geneLength);
        this.genes[nodeIndex].addInstance(serviceIndex);
    }

    public void afterBorn() {
        this.assignDemands();
    }

    private void moveOneInstance() {
        String instanceId, targetNodeId;
        do {
            int fromNodeIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(this.genes.length);
            int toNodeIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(this.genes.length);

            List<Integer> indicsWithInstance = new ArrayList<>();
            for (int i = 0; i < this.geneLength; ++i) {
                if (this.genes[fromNodeIndex].getGeneIntArr()[i] > 0) {
                    indicsWithInstance.add(i);
                }
            }
            int serviceIndex = indicsWithInstance.get(MGAUtils.MUTATION_SELECT_RAND.nextInt(indicsWithInstance.size()));
            this.genes[fromNodeIndex].deleteInstance(serviceIndex);
            this.genes[toNodeIndex].addInstance(serviceIndex);

            // re-assign user demands on this instance
            String serviceId = MWSGAPopulation.fixedServiceIdList.get(serviceIndex);
            List<MServiceInstance> instanceList = this.currOperator.getInstancesOfService(serviceId);
            int instanceIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(instanceList.size());
            instanceId = instanceList.get(instanceIndex).getId();
            targetNodeId = MWSGAPopulation.fixedNodeIdList.get(toNodeIndex);
        } while (!this.currOperator.moveInstance(instanceId, targetNodeId));
    }

    /**
     * In this function, we will assign demands to instances, and "save" non-feasible solution
     */
    private void assignDemands() {
        MDemandAssignHA.calc(this.unSolvedDemandList, currOperator);
        for (MBaseJob job : currOperator.getJobList()) {
            if (job.getType() == MJobType.DEPLOY) {
                MDeployJob deployJob = (MDeployJob) job;
                int nodeIndex = MWSGAPopulation.fixedNodeId2Index.get(deployJob.getServiceId());
                int serviceIndex = MWSGAPopulation.fixedServiceId2Index.get(deployJob.getNodeId());
                this.genes[nodeIndex].addInstance(serviceIndex);
            }
        }
        this.ifDemandsAssigned = true;
        this.unSolvedDemandList.clear();
    }

    /**
     * Init operator with current genes. Especially when a new chromosome is just born.
     * @param crossoverParent
     * @param cFromIndex
     * @param cToIndex
     */
    private List<MUserDemand> initGenes(MChromosome firstParent, MChromosome crossoverParent, int cFromIndex, int cToIndex) {
        // we will first check whether this solution consumes more resource than the available resource on the node
        // if it happens, an instance will be deleted randomly
        // when crossover happens, we also need to make sure that the instance id is exactly the same as the parents

        for (int nodeIndex = 0; nodeIndex < this.genes.length; ++nodeIndex) {
            String nodeId = MWSGAPopulation.fixedNodeIdList.get(nodeIndex);
            List<MServiceInstance> cParentInstanceList = crossoverParent.currOperator.getInstancesOnNode(nodeId);

            Map<String, List<MServiceInstance>> serviceId2InstanceList = new HashMap<>();
            for (MServiceInstance instance : cParentInstanceList) {
                if (!serviceId2InstanceList.containsKey(instance.getServiceId())) {
                    serviceId2InstanceList.put(instance.getServiceId(), new ArrayList<>());
                }
                serviceId2InstanceList.get(instance.getServiceId()).add(instance);
            }

            // get the instance list of the crossover parent from the crossover part
            //      and delete all the demands on the instances
            List<MServiceInstance> instanceList = new ArrayList<>();
            for (int serviceIndex = cFromIndex; serviceIndex < cToIndex; ++serviceIndex) {
                String serviceId = MWSGAPopulation.fixedServiceIdList.get(serviceIndex);
                List<String> idList = this.currOperator.getInstanceIdListOnNodeOfService(nodeId, serviceId);
                for (String instanceId : idList) {
                    this.currOperator.deleteInstance(instanceId);
                }
                instanceList.addAll(serviceId2InstanceList.get(serviceId));
            }

            // add instances of the crossover parent from the crossover part randomly
            Collections.shuffle(instanceList);
            int i = 0;
            for (; i < instanceList.size(); ++i) {
                if (this.currOperator.ifNodeHasResForIns(nodeId, instanceList.get(i).getServiceId())) {
                    this.currOperator.addNewInstance(instanceList.get(i).getServiceId(), nodeId, instanceList.get(i).getId());
                } else {
                    break;
                }
            }

            // and remove those which are not able to be deployed due to the limit of the resource
            for (; i < instanceList.size(); ++i) {
                this.genes[nodeIndex].deleteInstance(MWSGAPopulation.fixedServiceId2Index.get(instanceList.get(i).getServiceId()));
            }
        }

        // please remember, we still need to set the demands back to the instances that are deployed successfully
        return this.dealWithMappingCrossover(firstParent, crossoverParent, cFromIndex, cToIndex);
    }

    /**
     * This function does two thing:
     *   - for the demand mappings which are shown in both p1OldStates and p2OldStates, exchange them
     *   - for others, return them as un-solved demand
     * @param parent1: the main parent
     * @param parent2: the crossover parent
     */
    private List<MUserDemand> dealWithMappingCrossover(MChromosome parent1, MChromosome parent2, int fromIndex, int toIndex) {
        Map<String, MDemandState> oldStates = getDemandMappingFromGenes(parent1, fromIndex, toIndex);
        Map<String, MDemandState> otherOldStates = getDemandMappingFromGenes(parent2, fromIndex, toIndex);

        List<MUserDemand> child1UnSolvedStateList = new ArrayList<>();
        for (String demandId : oldStates.keySet()) {
            if (otherOldStates.containsKey(demandId)) {
                MDemandState oldState = oldStates.get(demandId);
                MUserDemand demand = MSystemModel.getIns().getUserManager().getUserDemandByUserAndDemandId(
                        oldState.getUserId(), oldState.getId()
                );
                MServiceInstance instance = this.currOperator.getInstanceById(otherOldStates.get(demandId).getInstanceId());
                if (instance != null) {
                    this.currOperator.assignDemandToIns(demand, instance, oldState);
                } else {
                    this.currOperator.removeDemandState(oldState);
                    child1UnSolvedStateList.add(demand);
                }
            }
        }
        return child1UnSolvedStateList;
    }

    /**
     * Get demand-instance mapping from given nodes(genes)
     */
    private static Map<String, MDemandState> getDemandMappingFromGenes(MChromosome chromosome, int fromIndex, int toIndex) {
        Map<String, MDemandState> demandStates = new HashMap<>();
        for (int nodeIndex = 0; nodeIndex < chromosome.genes.length; ++nodeIndex) {
            String nodeId = MBaseGA.fixedNodeIdList.get(nodeIndex);
            for (int i = fromIndex; i <= toIndex; ++i) {
                String serviceId = MBaseGA.fixedServiceIdList.get(i);
                List<String> serviceInstanceIds = chromosome.currOperator.getInstanceIdListOnNodeOfService(nodeId, serviceId);
                Set<String> instanceIdSet = new HashSet<>(serviceInstanceIds);
                demandStates.putAll(chromosome.currOperator.getDemandStateByInstanceIds(instanceIdSet));
            }
        }
        return demandStates;
    }

    public double getFitness() {
        if (this.fitness < 0) {
            this.fitness = this.currOperator.calcScore();
        }
        return this.fitness;
    }

    public double getCost() {
        if (this.cost < 0) {
            this.cost = this.currOperator.calcEvolutionCost(this.rawOperator);
        }
        return this.cost;
    }

    public double getWSGAFitness() {
        double score = this.getFitness();
        double cost = this.getCost();

        return MWSGAPopulation.W_SCORE * score * MWSGAPopulation.P_SCORE
                + MWSGAPopulation.W_COST * cost * MWSGAPopulation.P_COST;
    }

    public void calcWSGAFitness() {
        this.getWSGAFitness();
    }

    public void calcNSGAIIFitness() {
        this.getObjectiveValues();
    }

    public List<Double> getObjectiveValues() {
        List<Double> objectiveList = new ArrayList<>(2);
        objectiveList.add(this.getCost());
        objectiveList.add(this.getFitness());
        return objectiveList;
    }

    public void reset() {
        this.dominationCount = 0;
        this.rank = Integer.MAX_VALUE;
        this.dominatedChromosomes = new ArrayList<>();
    }

    public void setDominatedChromosome(MChromosome mChromosome) {
        this.dominatedChromosomes.add(mChromosome);
    }

    public void incrementDominationCount(int i) {
        this.dominationCount += i;
    }
}

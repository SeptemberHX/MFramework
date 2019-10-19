package com.septemberhx.server.adaptive.algorithm.ga;

import com.septemberhx.server.adaptive.algorithm.MDemandAssignHA;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.*;
import com.septemberhx.server.utils.MIDUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/4
 */
public class MChromosome {

    private static Logger logger = LogManager.getLogger(MChromosome.class);

    private MGene[] genes;
    private int geneLength;
    private MServerOperator rawOperator;

    @Getter
    private MServerOperator currOperator;
    private boolean ifDemandsAssigned = false;
    private List<MUserDemand> unSolvedDemandList = new ArrayList<>();
    private Random initRandom = new Random(1234567890);

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

    public static MChromosome randomInit(int nodeSize, int serviceSize, MServerOperator rawOperator, int maxInstanceNum) {
        MChromosome r = new MChromosome(nodeSize, serviceSize, rawOperator);

        // random init genes
        for (int n = 0; n < r.genes.length; ++n) {
            String nodeId = MBaseGA.fixedNodeIdList.get(n);
            Optional<MServerNode> nodeOpt = MSystemModel.getIns().getMSNManager().getById(nodeId);

            // the cloud server is not in the random init list;
            if (nodeOpt.isPresent() && nodeOpt.get().getNodeType() == ServerNodeType.CLOUD) continue;

            int iCount = maxInstanceNum;
            while (iCount-- > 0) {
                int sIndex = r.initRandom.nextInt(r.geneLength);
                String serviceId = MBaseGA.fixedServiceIdList.get(sIndex);

                if (r.currOperator.ifNodeHasResForIns(nodeId, serviceId)) {
                    r.currOperator.addNewInstance(serviceId, nodeId, MIDUtils.generateInstanceId(
                            nodeId, serviceId
                    ));
                    r.genes[n].addInstance(sIndex);
                } else {
                    break;
                }
            }
        }

        r.unSolvedDemandList = MSystemModel.getIns().getUserManager().getAllUserDemands();
        r.assignDemands();

        if (!r.verify()) {
            throw new RuntimeException("Illegal random init result");
        }

        return r;
    }

    public boolean verify() {
        if (!Configuration.DEBUG_MODE) {
            return true;
        }

        for (int n = 0; n < this.genes.length; ++n) {
            String nodeId = MBaseGA.fixedNodeIdList.get(n);
            for (int s = 0; s < this.geneLength; ++s) {
                String serviceId = MBaseGA.fixedServiceIdList.get(s);

                if (this.genes[n].getGeneIntArr()[s]
                        != this.currOperator.getInstanceIdListOnNodeOfService(nodeId, serviceId).size()) {
                    logger.error("Genes is not consistent with the solution");
                    return false;
                }
            }
        }

        if (MSystemModel.getIns().getUserManager().getAllUserDemands().size()
                != this.currOperator.getDemandStateManager().getAllValues().size()) {
            return false;
        }

        if (!this.currOperator.verify()) {
            throw new RuntimeException("Inconsistent left resource");
        }

        return true;
    }

    private MChromosome(int nodeSize, int serviceSize, MServerOperator rawOperator) {
        this.genes = new MGene[nodeSize];
        this.geneLength = serviceSize;
        for (int i = 0; i < nodeSize; ++i) {
            this.genes[i] = new MGene(serviceSize);
        }
        this.rawOperator = rawOperator;
        this.currOperator = MServerOperator.blankObject();
        this.reset();
    }

    public MChromosome(MGene[] genes, int geneLength, MServerOperator rawOperator) {
        this.genes = genes;
        this.geneLength = geneLength;
        this.rawOperator = rawOperator;
        this.currOperator = rawOperator.shallowClone();
        this.reset();
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

        if (Configuration.DEBUG_MODE) {
            logger.debug("Crossover: " + startIndex + "-" + endIndex);
            logger.debug("p1");
            this.printGenes();
            logger.debug("p2");
            other.printGenes();

            logger.debug("c1-raw");
            for (MGene gene : childGene1) {
                System.out.println(gene.toString());
            }

            logger.debug("c2-raw");
            for (MGene gene : childGene2) {
                System.out.println(gene.toString());
            }
        }


        // for the crossover on each node, the demand-instance mapping should be considered carefully
        // The exist mapping should be kept if possible, or mark as need-to-solved
        List<MChromosome> resultList = new ArrayList<>();
        MChromosome child1 = new MChromosome(childGene1, this.geneLength, this.rawOperator);
        child1.currOperator = this.currOperator.shallowClone();
        child1.initCrossoverGenes(this, other, startIndex, endIndex);

        MChromosome child2 = new MChromosome(childGene2, this.geneLength, this.rawOperator);
        child2.currOperator = other.currOperator.shallowClone();
        child2.initCrossoverGenes(other, this, startIndex, endIndex);

        if (Configuration.DEBUG_MODE) {
            logger.debug("c1");
            child1.printGenes();

            logger.debug("c2");
            child2.printGenes();
        }

        resultList.add(child1);
        resultList.add(child2);
        return resultList;
    }

    public void mutation() {
        double mutationType = MGAUtils.MUTATION_SELECT_RAND.nextDouble();

        if (Configuration.DEBUG_MODE) {
            logger.debug("Mutation rate: " + mutationType);
        }
        if (mutationType < 0.33) {              // add a new instance
            this.addOneInstance();
        } else if (mutationType < 0.66) {       // delete an exist instance
            this.deleteOneInstance();
        } else {                                // move an instance to other node
            this.moveOneInstance();
        }
    }

    public void printGenes() {
        for (MGene gene : this.genes) {
            System.out.println(gene.toString());
        }
    }

    private void deleteOneInstance() {
        List<MServiceInstance> allInstance = this.currOperator.getAllInstances();
        MServiceInstance randomInstance = allInstance.get(MGAUtils.MUTATION_SELECT_RAND.nextInt(allInstance.size()));
        int nodeIndex = MBaseGA.fixedNodeId2Index.get(randomInstance.getNodeId());
        int serviceIndex = MBaseGA.fixedServiceId2Index.get(randomInstance.getServiceId());
        this.genes[nodeIndex].deleteInstance(serviceIndex);

        // re-assign user demands on this instance
        List<MUserDemand> userDemands = this.currOperator.deleteInstance(randomInstance.getId());
        this.unSolvedDemandList.addAll(userDemands);
    }

    private void addOneInstance() {
        if (this.unSolvedDemandList.size() == 0) {
            return;
        }

        int nodeIndex, serviceIndex;
        String nodeId, serviceId;
        do {
            nodeIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(this.genes.length);
            serviceIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(this.geneLength);
            nodeId = MBaseGA.fixedNodeIdList.get(nodeIndex);
            serviceId = MBaseGA.fixedServiceIdList.get(serviceIndex);
        } while (!this.currOperator.ifNodeHasResForIns(nodeId, serviceId));
        this.currOperator.addNewInstance(serviceId, nodeId, MIDUtils.generateInstanceId(nodeId, serviceId));
        this.genes[nodeIndex].addInstance(serviceIndex);
    }

    public void afterBorn() {
        this.assignDemands();
        if (!this.verify()) {
            throw new RuntimeException("Illegal new-born result");
        }
    }

    private void moveOneInstance() {
        String instanceId, targetNodeId;
        int fromNodeIndex, toNodeIndex, serviceIndex;
        List<MServiceInstance> allInstance = this.currOperator.getAllInstances();
        do {
            MServiceInstance randomInstance = allInstance.get(MGAUtils.MUTATION_SELECT_RAND.nextInt(allInstance.size()));
            fromNodeIndex = MBaseGA.fixedNodeId2Index.get(randomInstance.getNodeId());
            serviceIndex = MBaseGA.fixedServiceId2Index.get(randomInstance.getServiceId());
            toNodeIndex = MGAUtils.MUTATION_SELECT_RAND.nextInt(this.genes.length);
            if (fromNodeIndex == toNodeIndex) return;

            // re-assign user demands on this instance
            targetNodeId = MWSGAPopulation.fixedNodeIdList.get(toNodeIndex);
            instanceId = randomInstance.getId();
        } while (!this.currOperator.moveInstance(instanceId, targetNodeId));
        this.genes[fromNodeIndex].deleteInstance(serviceIndex);
        this.genes[toNodeIndex].addInstance(serviceIndex);
    }

    /**
     * In this function, we will assign demands to instances, and "save" non-feasible solution
     */
    private void assignDemands() {
        List<MBaseJob> newJobList = MDemandAssignHA.calc(this.unSolvedDemandList, currOperator);
        for (MBaseJob job : newJobList) {
            if (job.getType() == MJobType.DEPLOY) {
                MDeployJob deployJob = (MDeployJob) job;
                int nodeIndex = MWSGAPopulation.fixedNodeId2Index.get(deployJob.getNodeId());
                int serviceIndex = MWSGAPopulation.fixedServiceId2Index.get(deployJob.getServiceId());
                this.genes[nodeIndex].addInstance(serviceIndex);
            } else if (job.getType() == MJobType.DELETE) {
                MDeleteJob deleteJob = (MDeleteJob) job;
                int nodeIndex = MWSGAPopulation.fixedNodeId2Index.get(deleteJob.getServiceId());
                int serviceIndex = MWSGAPopulation.fixedServiceId2Index.get(deleteJob.getNodeId());
                this.genes[nodeIndex].addInstance(serviceIndex);
            }
        }
        this.ifDemandsAssigned = true;
        this.unSolvedDemandList.clear();

        this.currOperator.adjustJobList();  // remove useless jobs

        // For deploy job. Some times the deployed instance will not used. So we will delete it
        List<MBaseJob> jobList = this.currOperator.getJobList();
        Iterator<MBaseJob> jobIterator = jobList.iterator();
        List<MServiceInstance> emptyInstanceList = new ArrayList<>();
        while (jobIterator.hasNext()) {
            MBaseJob currJob = jobIterator.next();
            if (currJob.getType() == MJobType.DEPLOY) {
                MDeployJob deployJob = (MDeployJob) currJob;
                if (this.currOperator.getInstanceById(deployJob.getInstanceId()) == null) {
                    jobIterator.remove();
                    continue;
                }

                if (this.currOperator.getInstanceUserNumber(deployJob.getInstanceId()) == 0) {
                    MServiceInstance instance = this.currOperator.getInstanceById(((MDeployJob) currJob).getInstanceId());
                    emptyInstanceList.add(instance);
                    jobIterator.remove();
                }

                // if the job try to deploy an instance that exists before, we will remove it
                if (this.rawOperator.getInstanceById(deployJob.getInstanceId()) != null) {
                    jobIterator.remove();
                }
            } else if (currJob.getType() == MJobType.DELETE) {
                // if the job tries to remove an instance that not exists before, we will remove it
                if (this.rawOperator.getInstanceById(((MDeleteJob) currJob).getInstanceId()) == null) {
                    jobIterator.remove();
                }
            } else if (currJob.getType() == MJobType.MOVE) {
                // if the job tries to move an instance to the same node, we will remove it
                MMoveJob moveJob = (MMoveJob) currJob;
                if (this.rawOperator.getInstanceById(moveJob.getInstanceId()).getNodeId().equals(moveJob.getTargetNodeId())) {
                    jobIterator.remove();
                }
            }
        }

        for (MServiceInstance instance : emptyInstanceList) {
            if (this.rawOperator.getInstanceById(instance.getId()) != null) {
                this.currOperator.deleteInstance(instance.getId());
            } else {
                this.currOperator.deleteInstance(instance.getId(), false);
            }
            int nodeIndex = MBaseGA.fixedNodeId2Index.get(instance.getNodeId());
            int serviceIndex = MBaseGA.fixedServiceId2Index.get(instance.getServiceId());
            this.genes[nodeIndex].deleteInstance(serviceIndex);
        }
    }

    /**
     * Init operator with current genes. Especially when a new chromosome is just born.
     * @param crossoverParent
     * @param cFromIndex
     * @param cToIndex
     */
    private void initCrossoverGenes(MChromosome firstParent, MChromosome crossoverParent, int cFromIndex, int cToIndex) {
        // we will first check whether this solution consumes more resource than the available resource on the node
        // if it happens, an instance will be deleted randomly
        // when crossover happens, we also need to make sure that the instance id is exactly the same as the parents

        if (Configuration.DEBUG_MODE) {
            logger.debug("Check if the left resource is consistent in operator before initCrossoverGenes: " + this.currOperator.verify());
        }

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
            for (int serviceIndex = cFromIndex; serviceIndex <= cToIndex; ++serviceIndex) {
                String serviceId = MWSGAPopulation.fixedServiceIdList.get(serviceIndex);
                List<String> idList = this.currOperator.getInstanceIdListOnNodeOfService(nodeId, serviceId);
                for (String instanceId : idList) {
                    this.currOperator.deleteInstance(instanceId);
                }

                if (serviceId2InstanceList.containsKey(serviceId)) {
                    instanceList.addAll(serviceId2InstanceList.get(serviceId));
                }
            }

            // add instances of the crossover parent from the crossover part
            int i = 0;
            for (; i < instanceList.size(); ++i) {
                if (this.currOperator.ifNodeHasResForIns(nodeId, instanceList.get(i).getServiceId())) {
                    this.currOperator.addNewInstance(instanceList.get(i).getServiceId(), nodeId, instanceList.get(i).getId());
                } else {
                    throw new RuntimeException("No enough resource during initCrossoverGenes");
                }
            }
        }

        if (Configuration.DEBUG_MODE) {
            logger.debug("Check if the left resource is consistent in operator after initCrossoverGenes: " + this.currOperator.verify());
        }

        // please remember, we still need to set the demands back to the instances that are deployed successfully
        this.unSolvedDemandList.addAll(this.dealWithMappingCrossover(firstParent, crossoverParent, cFromIndex, cToIndex));
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
            MDemandState oldState = oldStates.get(demandId);
            MUserDemand demand = MSystemModel.getIns().getUserManager().getUserDemandByUserAndDemandId(
                    oldState.getUserId(), oldState.getId()
            );
            if (otherOldStates.containsKey(demandId)) {

                MServiceInstance instance = this.currOperator.getInstanceById(otherOldStates.get(demandId).getInstanceId());
                if (instance != null) {
                    this.currOperator.assignDemandToIns(demand, instance, oldState);
                } else {
                    this.currOperator.removeDemandState(oldState);
                    child1UnSolvedStateList.add(demand);
                }
            } else {
                child1UnSolvedStateList.add(demand);
            }
        }

        if (Configuration.DEBUG_MODE) {
            Set<String> oldStateIdSet = oldStates.keySet();
            oldStateIdSet.removeAll(otherOldStates.keySet());
            logger.debug("Check if unSolvedStateList size is right after dealWithMappingCrossover: " + (oldStateIdSet.size() == child1UnSolvedStateList.size()));
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
//            this.cost = this.currOperator.calcEvolutionCost(this.rawOperator);
            this.cost = this.currOperator.calcEvolutionCost();
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

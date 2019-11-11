package com.septemberhx.server.adaptive.algorithm;

import com.septemberhx.server.adaptive.algorithm.ga.*;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/4
 *
 * This class performs the major evolution algorithm.
 * Considering that assigning demands to instance and assigning instances to nodes are both variables, it is not likely
 *   solved by linear algorithm due to the non-linear cost function.
 * Here, we will use evolution algorithms instead of linear one.
 */
public class MMajorAlgorithm implements MAlgorithmInterface {

    public enum GA_TYPE {
        WSGA,
        NSGA_II,
        MODE_A
    }

    private static Logger logger = LogManager.getLogger(MMajorAlgorithm.class);
    private GA_TYPE gaType = GA_TYPE.WSGA;

    public MMajorAlgorithm(GA_TYPE type) {
        this.gaType = type;
    }

    public MMajorAlgorithm() {
        this.gaType = GA_TYPE.WSGA;
    }

    @Override
    public MPlannerResult calc(MAnalyserResult data, MServerOperator rawOperator) {
        // First, init operator
        MServerOperator serverOperator = MSystemModel.getIns().getOperator();
        serverOperator.reInit();
        // Then, do the composition job behind initialization. It will modify system model by operator
        MCompositionAlgorithmInCommon.doCompositionPart(data.getCallGraph(), rawOperator, serverOperator);
        if (Configuration.COMPOSITION_ALL_ENABLED) {
            MCompositionAlgorithmInCommon.doCompositionPart(data.getAllCallGraph(), rawOperator, serverOperator);
        }
        MCompositionAlgorithmInCommon.initPotentialPairList(
                MSystemModel.getIns().getUserManager().getAllUserDemands(),
                serverOperator.getServiceManager(),
                MSystemModel.getIns().getUserManager()
        );
        // DO NOT CHANGE THE ORDER ABOVE.

        // Due to the huge amount of the user demands, it's not likely to put demand-instance mapping in the result of
        //    each evolution(like GA, ABC and so on). It will cause low efficiency when generate children. So we will
        //    split it into two parts:
        //
        //    Part 1. Use evolution algorithm to generate instance placement
        //    Part 2. Assign demands to the placement generated in Part 1
        //    Part 3. Compose results of Part 1 and Part 2 together as one solution
        //
        // Only the instance placement will be directly changed by mutation/crossover
        MBaseGA ga = this.getGAObject(serverOperator, rawOperator);
        MServerOperator operator = ga.evolve();

        MPlannerResult plannerResult = new MPlannerResult();
        plannerResult.setServerOperator(operator);
        return plannerResult;
    }

    private MBaseGA getGAObject(MServerOperator operator, MServerOperator rawOperator) {
        switch (this.gaType) {
            case WSGA:
                return new MWSGAPopulation(operator, rawOperator);
            case MODE_A:
                return new MMOEADPopulation(operator, rawOperator);
            case NSGA_II:
                return new MNSGAIIPopulation(operator, rawOperator);
        }
        return new MWSGAPopulation(operator, rawOperator);
    }
}

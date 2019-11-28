package com.septemberhx.server.adaptive;


import com.septemberhx.server.adaptive.algorithm.MAlgorithmInterface;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;

/**
 * make a plan of the evolution for the input info
 */
public class MPlanner {

    public MPlannerResult plan(MAnalyserResult info, MAlgorithmInterface algorithm) {
        MServerOperator rawOperator = MSystemModel.getIns().getOperator().shallowClone();
        rawOperator.reInit();
        return algorithm.calc(info, rawOperator);
    }
}

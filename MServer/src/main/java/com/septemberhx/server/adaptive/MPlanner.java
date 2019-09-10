package com.septemberhx.server.adaptive;


import com.septemberhx.server.adaptive.algorithm.MAlgorithmInterface;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;

/**
 * make a plan of the evolution for the input info
 */
public class MPlanner {

    public MPlannerResult plan(MAnalyserResult info, MAlgorithmInterface algorithm) {
        return algorithm.calc(info);
    }
}

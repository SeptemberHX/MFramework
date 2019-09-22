package com.septemberhx.server.adaptive.algorithm;

import com.google.common.graph.EndpointPair;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.base.model.MSIInterface;

import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/22
 */
public class MMinorAlgorithm implements MAlgorithmInterface {
    @Override
    public MPlannerResult calc(MAnalyserResult data) {
        List<EndpointPair<MSIInterface>> pCompositionList = data.getPotentialCompositionList();


        return null;
    }
}

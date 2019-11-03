package com.septemberhx.server.adaptive;

import com.septemberhx.server.adaptive.algorithm.MAlgorithmInterface;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MServerOperator;

public class MBaseAlgorithm implements MAlgorithmInterface {
    @Override
    public MPlannerResult calc(MAnalyserResult data, MServerOperator rawOperator) {
        return null;
    }
}

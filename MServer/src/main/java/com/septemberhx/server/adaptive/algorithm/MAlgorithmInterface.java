package com.septemberhx.server.adaptive.algorithm;

import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MServerOperator;

public interface MAlgorithmInterface {
    public MPlannerResult calc(MAnalyserResult data, MServerOperator rawOperator);
}

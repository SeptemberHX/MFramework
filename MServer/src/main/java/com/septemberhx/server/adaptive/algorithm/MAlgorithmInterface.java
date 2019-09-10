package com.septemberhx.server.adaptive.algorithm;

import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;

public interface MAlgorithmInterface {
    public MPlannerResult calc(MAnalyserResult data);
}

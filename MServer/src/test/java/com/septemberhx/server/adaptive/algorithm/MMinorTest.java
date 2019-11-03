package com.septemberhx.server.adaptive.algorithm;

import com.septemberhx.server.adaptive.MAnalyser;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.utils.MDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/16
 */
public class MMinorTest {

    @Test
    void calc() {
        MAnalyser analyser = new MAnalyser();
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>(), new ArrayList<>());

        MMinorAlgorithm minorAlgorithm = new MMinorAlgorithm();
        minorAlgorithm.calc(analyserResult, new MServerOperator());
    }

    @BeforeEach
    void setUp() {
        MDataUtils.loadDataFromDir("D:\\Workspace\\gitlab\\mdata\\Lab2\\TestData", true);
    }
}

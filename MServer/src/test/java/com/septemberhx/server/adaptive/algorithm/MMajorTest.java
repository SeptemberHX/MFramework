package com.septemberhx.server.adaptive.algorithm;

import com.septemberhx.server.adaptive.MAnalyser;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.utils.MDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/17
 */
class MMajorTest {

    @Test
    void wsga() {
        MAnalyser analyser = new MAnalyser();
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>(), new ArrayList<>());

        MMajorAlgorithm mMajorAlgorithm = new MMajorAlgorithm(MMajorAlgorithm.GA_TYPE.WSGA);
        mMajorAlgorithm.calc(analyserResult);
    }

    @Test
    void nsga_ii() {
        MAnalyser analyser = new MAnalyser();
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>(), new ArrayList<>());

        MMajorAlgorithm mMajorAlgorithm = new MMajorAlgorithm(MMajorAlgorithm.GA_TYPE.NSGA_II);
        mMajorAlgorithm.calc(analyserResult);
    }

    @Test
    void moea_d() {
        MAnalyser analyser = new MAnalyser();
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>(), new ArrayList<>());

        MMajorAlgorithm mMajorAlgorithm = new MMajorAlgorithm(MMajorAlgorithm.GA_TYPE.MODE_A);
        mMajorAlgorithm.calc(analyserResult);
    }

    @BeforeEach
    void setUp() {
        MDataUtils.loadDataFromDir("D:\\Workspace\\gitlab\\mdata\\Lab2\\TestData");
    }
}
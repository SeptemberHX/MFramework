package com.septemberhx.server;

import com.septemberhx.server.adaptive.MAnalyser;
import com.septemberhx.server.adaptive.algorithm.MMajorAlgorithm;
import com.septemberhx.server.adaptive.algorithm.MMinorAlgorithm;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.utils.MDataUtils;

import java.util.ArrayList;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/10/25
 */
public class MExperiment {

    public static void calcPrevSystemState(String dataDirPath, String prevSystemFilePath) {
        // load all data into MSystemModel
        MDataUtils.loadDataFromDir(dataDirPath, true);

        // create previous placement and assignment with heuristic algorithm
        MAnalyser analyser = new MAnalyser();
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>(), new ArrayList<>());

        MMinorAlgorithm minorAlgorithm = new MMinorAlgorithm();
        MPlannerResult result = minorAlgorithm.calc(analyserResult, MSystemModel.getIns().getOperator().shallowClone());

        // save the previous data into file
        // we will dump MServerOperator into json file
        MDataUtils.saveServerOperatorToFile(result.getServerOperator(), prevSystemFilePath);
    }

    public static void test(String dataDirPath) {
        MDataUtils.loadDataFromDir(dataDirPath, false);

        // You should consider which part of the data is needed to load into the system
        MServerOperator serverOperator = MDataUtils.loadServerOperator(dataDirPath + "/prev_system.json");
        serverOperator.printStatus();

        MAnalyser analyser = new MAnalyser();
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>(), new ArrayList<>());

        MMajorAlgorithm mMajorAlgorithm = new MMajorAlgorithm(MMajorAlgorithm.GA_TYPE.WSGA);
        mMajorAlgorithm.calc(analyserResult, serverOperator);

    }

    public static void main(String[] args) {
//        calcPrevSystemState("D:\\Workspace\\gitlab\\mdata\\Lab2\\ExperimentData", "D:\\Workspace\\gitlab\\mdata\\Lab2\\ExperimentData\\prev_system.json");
        test("D:\\Workspace\\gitlab\\mdata\\Lab2\\ExperimentData");
    }

}

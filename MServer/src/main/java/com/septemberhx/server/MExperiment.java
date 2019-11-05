package com.septemberhx.server;

import com.septemberhx.server.adaptive.MAnalyser;
import com.septemberhx.server.adaptive.algorithm.MMajorAlgorithm;
import com.septemberhx.server.adaptive.algorithm.MMinorAlgorithm;
import com.septemberhx.server.adaptive.algorithm.ga.Configuration;
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
        MAnalyser analyser = new MAnalyser(new MServerOperator());
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>(), new ArrayList<>());

        MMinorAlgorithm minorAlgorithm = new MMinorAlgorithm();
        MPlannerResult result = minorAlgorithm.calc(analyserResult, MSystemModel.getIns().getOperator().shallowClone());
        if (!result.getServerOperator().verify()) {
            throw new RuntimeException("Error in calcPrevSystemState");
        }
        // save the previous data into file
        // we will dump MServerOperator into json file
        MDataUtils.saveServerOperatorToFile(result.getServerOperator(), prevSystemFilePath);
    }

    public static void runExperiment(String dataDirPath, MMajorAlgorithm.GA_TYPE gaType) {
        MDataUtils.loadDataFromDir(dataDirPath, false);

        // You should consider which part of the data is needed to load into the system
        MServerOperator serverOperator = MDataUtils.loadServerOperator(dataDirPath + "/prev_system.json");
        serverOperator.printStatus();

        MAnalyser analyser = new MAnalyser(serverOperator);
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>(), new ArrayList<>());

        MMajorAlgorithm mMajorAlgorithm = new MMajorAlgorithm(gaType);
        mMajorAlgorithm.calc(analyserResult, serverOperator);

    }

    public static void main(String[] args) {
        System.out.println("====== Start with data: " + args[0]
                + " with algorithm " + args[1]
                + " with max-round " + args[2]
                + " with population " + args[3]
                + ", compositionEnabled = " + args[4]);
//        calcPrevSystemState("D:\\Workspace\\gitlab\\mdata\\Lab2\\ExperimentData", "D:\\Workspace\\gitlab\\mdata\\Lab2\\ExperimentData\\prev_system.json");
        MMajorAlgorithm.GA_TYPE gaType = MMajorAlgorithm.GA_TYPE.WSGA;
        Configuration.POPULATION_SIZE = Integer.parseInt(args[3]);
        Configuration.COMPOSITION_ALL_ENABLED = Boolean.parseBoolean(args[4]);
        switch (args[1]) {
            case "wsga":
                gaType = MMajorAlgorithm.GA_TYPE.WSGA;
                Configuration.WSGA_MAX_ROUND = Integer.parseInt(args[2]);
                break;
            case "nsgaii":
                gaType = MMajorAlgorithm.GA_TYPE.NSGA_II;
                Configuration.NSGAII_MAX_ROUND = Integer.parseInt(args[2]);
                break;
            case "moead":
                gaType = MMajorAlgorithm.GA_TYPE.MODE_A;
                Configuration.MOEAD_MAX_ROUND = Integer.parseInt(args[2]);
                break;
            default:
                break;
        }
        runExperiment(args[0], gaType);
    }

}

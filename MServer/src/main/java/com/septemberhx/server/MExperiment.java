package com.septemberhx.server;

import com.septemberhx.common.base.MUser;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.adaptive.MAnalyser;
import com.septemberhx.server.adaptive.algorithm.MMajorAlgorithm;
import com.septemberhx.server.adaptive.algorithm.MMinorAlgorithm;
import com.septemberhx.server.adaptive.algorithm.ga.Configuration;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.core.MUserManager;
import com.septemberhx.server.utils.MDataUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

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
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>());

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
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>());

        MMajorAlgorithm mMajorAlgorithm = new MMajorAlgorithm(gaType);
        mMajorAlgorithm.calc(analyserResult, serverOperator);

    }

    public static void main(String[] args) {
        majorMain(args);
    }

    public static void majorMain(String[] args) {
//        String data_version = "common";
//        for (double i = 0.15; i <= 0.5; i+=0.1) {
//            calcPrevSystemState(
//                String.format("D:\\Workspace\\gitlab\\mdata\\Lab2\\ExperimentData\\%s\\%.2f", data_version, i),
//                String.format("D:\\Workspace\\gitlab\\mdata\\Lab2\\ExperimentData\\%s\\%.2f\\prev_system.json", data_version, i)
//            );
//        }
//        MMajorAlgorithm.GA_TYPE gaType = prepareParameters(args);
//        runExperiment(args[0], gaType);
//        calcPrevSystemState("/media/septemberhx/新加卷/Workspace/gitlab/mdata/Lab2/ExperimentData/switch/base/",
//                "/media/septemberhx/新加卷/Workspace/gitlab/mdata/Lab2/ExperimentData/switch/base/prev_system.json");
        minor2MajorMain(args);
    }

    public static void minor2MajorMain(String[] args) {
        // default configuration
        Configuration.POPULATION_SIZE = 100;
        Configuration.COMPOSITION_ALL_ENABLED = true;
        Configuration.VERIFY_EVERY_CHILD = false;
        MAdaptiveSystem.COMPOSITION_THRESHOLD = 0.01;
        MMajorAlgorithm.GA_TYPE gaType = MMajorAlgorithm.GA_TYPE.NSGA_II;
        Configuration.NSGAII_MAX_ROUND = 200;

        // parameters that control the switch between the minor and major
        runExperiment2(args[0], args[1], gaType, Double.parseDouble(args[2]));
    }

    public static void runExperiment2(String basicDirPath, String nextDirPath, MMajorAlgorithm.GA_TYPE gaType, double tolerance) {
        MDataUtils.loadDataFromDir(basicDirPath, false);

        // You should consider which part of the data is needed to load into the system
        MServerOperator serverOperator = MDataUtils.loadServerOperator(basicDirPath + "/prev_system.json");
        serverOperator.printStatus();

        // read next demands lists from nextDirPath which will have a list of demand{NO}.json
        Map<Integer, MUserManager> userManagerMap = MDataUtils.loadUserManagerMap(nextDirPath);
        List<Integer> orderedTimeList = new ArrayList<>(userManagerMap.keySet());
        Collections.sort(orderedTimeList);

        // fetch the parameters
        int maxMinorCount = 50;
        double downTolerance = tolerance;

        // keep doing the evolution
        System.out.println("Experiment output 1: " + serverOperator.calcScore_v2() + ", " + System.currentTimeMillis());
        long startTimestamp = System.currentTimeMillis();
        boolean expEnd = false;
        int currIndex = 0;
        double lastResponseTime = serverOperator.calcScore_v2();
        int minorCount = 0;
//        while (!expEnd) {
//            long currTimestamp = System.currentTimeMillis();
//            if (currTimestamp - startTimestamp < orderedTimeList.get(currIndex) * 1000) {
//                try {
//                    Thread.sleep(1000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                ;
//            }
//        }

        for (int i = 0; i < orderedTimeList.size(); ++i) {
            MAnalyser analyser = new MAnalyser(serverOperator);
            MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>());
            MServerOperator oldOperator = MSystemModel.getIns().getOperator();
            System.out.println("Experiment affected demand size: " + affectedDemandSize(MSystemModel.getIns().getUserManager(), userManagerMap.get(orderedTimeList.get(currIndex))));
            MSystemModel.getIns().setUserManager(userManagerMap.get(orderedTimeList.get(i)));
            MServerOperator nextOperator;
            double nextDelay = 0;
            System.out.println("Experiment start: " + System.currentTimeMillis());
            if (minorCount < maxMinorCount) {
                minorCount += 1;
                MMinorAlgorithm minorAlgorithm = new MMinorAlgorithm();
                nextOperator = minorAlgorithm.calc(analyserResult, oldOperator).getServerOperator();
                nextDelay = nextOperator.calcScore_v2();
                System.out.println("Experiment use minor: " + nextDelay);
                if (nextDelay - lastResponseTime > lastResponseTime * downTolerance) {
                    MMajorAlgorithm majorAlgorithm = new MMajorAlgorithm(MMajorAlgorithm.GA_TYPE.WSGA);
                    nextOperator = majorAlgorithm.calc(analyserResult, oldOperator).getServerOperator();
                    nextDelay = nextOperator.calcScore_v2();
                    System.out.println("Experiment use major after minor: " + nextDelay);
                    minorCount = 0;
                }
            } else {
                minorCount = 0;
                MMajorAlgorithm majorAlgorithm = new MMajorAlgorithm(MMajorAlgorithm.GA_TYPE.WSGA);
                nextOperator = majorAlgorithm.calc(analyserResult, oldOperator).getServerOperator();
                nextDelay = nextOperator.calcScore_v2();
                System.out.println("Experiment use major: " + nextDelay);
            }
            System.out.println("Experiment output: " + nextOperator.calcScore_v2() + ", " + System.currentTimeMillis());
            if (nextDelay < lastResponseTime) {
                lastResponseTime = nextDelay;
            }
            MSystemModel.getIns().setOperator(nextOperator);
            MSystemModel.getIns().setServiceManager(nextOperator.getServiceManager().shallowClone());
//            currIndex += 1;
        }

        MAnalyser analyser = new MAnalyser(serverOperator);
        MAnalyserResult analyserResult = analyser.analyse(new ArrayList<>());
    }

    private static int affectedDemandSize(MUserManager oldUserManager, MUserManager currUserManager) {
        Set<String> oldUserDemandIdSet = new HashSet<>();
        for (MUserDemand userDemand : oldUserManager.getAllUserDemands()) {
            oldUserDemandIdSet.add(userDemand.getId());
        }

        Set<String> newUserDemandIdSet = new HashSet<>();
        int newDemandSize = 0;
        for (MUserDemand userDemand : currUserManager.getAllUserDemands()) {
            if (!oldUserDemandIdSet.contains(userDemand.getId())) {
                newDemandSize += 1;
            }
            newUserDemandIdSet.add(userDemand.getId());
        }

        oldUserDemandIdSet.removeAll(newUserDemandIdSet);
        int removedDemandSize = oldUserDemandIdSet.size();

        return newDemandSize + removedDemandSize;
    }

    private static MMajorAlgorithm.GA_TYPE prepareParameters(String[] args) {
        System.out.println("====== Start with data: " + args[0]
                + " with algorithm " + args[1]
                + " with max-round " + args[2]
                + " with population " + args[3]
                + ", compositionEnabled = " + args[4]
                + ", verify children = " + args[5]
                + ", composition threshold = " + args[6]
        );
        Configuration.POPULATION_SIZE = Integer.parseInt(args[3]);
        Configuration.COMPOSITION_ALL_ENABLED = Boolean.parseBoolean(args[4]);
        Configuration.VERIFY_EVERY_CHILD = Boolean.parseBoolean(args[5]);
        MAdaptiveSystem.COMPOSITION_THRESHOLD = Double.parseDouble(args[6]);
        MMajorAlgorithm.GA_TYPE gaType = MMajorAlgorithm.GA_TYPE.WSGA;
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
        return gaType;
    }
}

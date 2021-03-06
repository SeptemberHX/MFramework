package com.septemberhx.server.adaptive;

import com.septemberhx.common.base.MUser;
import com.septemberhx.common.log.MBaseLog;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.adaptive.algorithm.MAlgorithmInterface;
import com.septemberhx.server.adaptive.algorithm.MEvolveType;
import com.septemberhx.server.adaptive.algorithm.MMajorAlgorithm;
import com.septemberhx.server.adaptive.algorithm.MMinorAlgorithm;
import com.septemberhx.server.adaptive.executor.MBuildExecutor;
import com.septemberhx.server.adaptive.executor.MClusterExecutor;
import com.septemberhx.server.adaptive.executor.MExecutor;
import com.septemberhx.server.base.MAnalyserInput;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MSystemModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The main part of the self-adaptive system.
 * It follows the standard system design: monitor, analyser, planner and executor
 */
public class MAdaptiveSystem {

    private static Logger logger = LogManager.getLogger(MAdaptiveSystem.class);
    private MMonitor monitor;
    private MAnalyser analyser;
    private MPlanner planner;
    private MExecutor executor;
    private MAlgorithmInterface algorithm;
    private MSystemState state;

    public static Double MINOR_THRESHOLD = 0.0;     // when there are users not satisfied, we will use minor
    public static Double MAJOR_THRESHOLD = 0.1;     // when 10% users are not satisfied, we use major

    public static Double COMPOSITION_THRESHOLD = 0.015;
    public static Long MAX_DELAY_TOLERANCE = 300L;
    public static Long UNAVAILABLE_TOLERANCE = 3000L;
    public static Long UNAVAILABLE_TRANSFORM_TIME = 1000000L;

    public static double ALPHA = 0.1;
    public static int timeIntervalInMin = 1;

    public MAdaptiveSystem() {
        this.monitor = new MMonitor();
        this.analyser = new MAnalyser(MSystemModel.getIns().getOperator());
        this.planner = new MPlanner();

        MClusterExecutor clusterExecutor = new MClusterExecutor();
        MBuildExecutor buildExecutor = new MBuildExecutor();
        this.executor = new MExecutor(clusterExecutor, buildExecutor);

        this.algorithm = new MBaseAlgorithm();

        this.turnToMonitor();
    }

    /**
     * The main work flow of the self-adaptive system.
     */
    public void evolve(MEvolveType type) {
        // fetch the logs from the cluster
        DateTime now = DateTime.now();
        MAnalyserInput analyserInput = this.monitor.monitor(now.minusMinutes(timeIntervalInMin), now);

        // replace the users we just got from the cluster
        MSystemModel.getIns().getUserManager().clear();
        for (MUser user : analyserInput.getUserList()) {
            MSystemModel.getIns().getUserManager().add(user);
        }

        // analyze the system to check whether an evolution is needed
        MAnalyserResult analyserResult = this.analyze(analyserInput);
        if (analyserResult.getEvolveType() == MEvolveType.NO_NEED) {
            return;
        }

        if (type != MEvolveType.NO_NEED) {
            analyserResult.setEvolveType(type);
        }

        // do the plan until successfully get the plan which meets the requirements defined in the MPlanner
        MPlannerResult result = this.plan(analyserResult);
//        while (!result.isSuccess()) {
//            analyserResult = this.analyze();
//            if (analyserResult.getEvolveType() == MEvolveType.NO_NEED) {
//                logger.warn("Evolve type cannot be NO_NEED in the while loop!");
//            }
//            result = this.plan(analyserResult);
//        }

        // execute the evolution plan
        this.evolve(result);
    }

    /**
     * Accept the service log and decide whether to do the analyse or not
     * @param serviceLog void
     */
    public void acceptServiceLog(MServiceBaseLog serviceLog) {
        this.monitor.acceptLog(serviceLog);

        // todo: conditions to decide whether to do the analyse
        this.evolve(MEvolveType.NO_NEED);
    }

    /**
     * Get the logs according to the time window defined in the MAnalyser from MMonitor
     * @return MAnalyserResult: the result of the analyzing
     */
    private MAnalyserResult analyze(MAnalyserInput analyserInput) {
        MAnalyser analyser = this.turnToAnalyser();

        List<MServiceBaseLog> logList = new ArrayList<>();
        for (MBaseLog baseLog : analyserInput.getLogList()) {
            if (baseLog instanceof  MServiceBaseLog) {
                logList.add((MServiceBaseLog) baseLog);
            }
        }
        return analyser.analyse(logList);
    }

    /**
     * Calculate for the plan of the evolution given to the analyzing result from MAnalyser
     * @param analyserResult: the result of analyze()
     * @return MPlannerResult: the result of the planning
     */
    private MPlannerResult plan(MAnalyserResult analyserResult) {
        MPlanner planner = this.turnToPlanner();
        if (analyserResult.getEvolveType() == MEvolveType.MINOR) {
            return planner.plan(analyserResult, new MMinorAlgorithm());
        } else if (analyserResult.getEvolveType() == MEvolveType.MAJOR) {
            return planner.plan(analyserResult, new MMajorAlgorithm());
        } else {
            return new MPlannerResult();
        }
    }

    /**
     * Execute the evolution plane
     * @param result void
     */
    private void evolve(MPlannerResult result) {
        MExecutor mainExecutor = this.turnToExecutor();
        mainExecutor.execute(result);
    }

    /**
     * Start the self-adaptive system. It will collect logs and do the analyse jobs when necessary
     */
    public void start() {
        // todo: get data from elasticsearch
    }

    private MMonitor turnToMonitor() {
        this.state = MSystemState.MONITING;
        return this.monitor;
    }

    private MAnalyser turnToAnalyser() {
        this.state = MSystemState.ANALYZING;
        return this.analyser;
    }

    private MPlanner turnToPlanner() {
        this.state = MSystemState.PLANNING;
        return this.planner;
    }

    private MExecutor turnToExecutor() {
        this.state = MSystemState.EXECUTING;
        return this.executor;
    }
}

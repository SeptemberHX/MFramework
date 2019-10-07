package com.septemberhx.server.adaptive;

import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.adaptive.algorithm.MAlgorithmInterface;
import com.septemberhx.server.adaptive.algorithm.MEvolveType;
import com.septemberhx.server.adaptive.executor.MBuildExecutor;
import com.septemberhx.server.adaptive.executor.MClusterExecutor;
import com.septemberhx.server.adaptive.executor.MExecutor;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MSystemModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.List;

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

    public static Double COMPOSITION_THRESHOLD = 0.2;
    public static Long MAX_DELAY_TOLERANCE = 300L;
    public static Long UNAVAILABLE_TOLERANCE = 3000L;
    public static Long UNAVAILABLE_TRANSFORM_TIME = 1000000L;

    public static double ALPHA = 0.1;

    public MAdaptiveSystem() {
        this.monitor = new MMonitor();
        this.analyser = new MAnalyser();
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
    public void evolve() {
        // analyze the system to check whether an evolution is needed
        MAnalyserResult analyserResult = this.analyze();
        if (analyserResult.getEvolveType() == MEvolveType.NO_NEED) {
            return;
        }

        // do the plan until successfully get the plan which meets the requirements defined in the MPlanner
        MPlannerResult result = this.plan(analyserResult);
        while (!result.isSuccess()) {
            // todo: collect the data from the 'result' and pass it to analyzer
            analyserResult = this.analyze();
            if (analyserResult.getEvolveType() == MEvolveType.NO_NEED) {
                logger.warn("Evolve type cannot be NO_NEED in the while loop!");
            }
            result = this.plan(analyserResult);
        }

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
        this.evolve();
    }

    /**
     * Get the logs according to the time window defined in the MAnalyser from MMonitor
     * @return MAnalyserResult: the result of the analyzing
     */
    private MAnalyserResult analyze() {
        MAnalyser analyser = this.turnToAnalyser();
        DateTime logEndTime = DateTime.now();
        DateTime logStartTime = logEndTime.minus(analyser.getTimeWindowInMillis());
        List<MServiceBaseLog> logList = this.monitor.getLogBetweenDateTime(logStartTime, logEndTime);
        return analyser.analyse(logList, MSystemModel.getIns().getDemandStateManager().getAllValues());
    }

    /**
     * Calculate for the plan of the evolution given to the analyzing result from MAnalyser
     * @param analyserResult: the result of analyze()
     * @return MPlannerResult: the result of the planning
     */
    private MPlannerResult plan(MAnalyserResult analyserResult) {
        MPlanner planner = this.turnToPlanner();
        return planner.plan(analyserResult, this.algorithm);
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

package com.septemberhx.server.adaptive;


import com.septemberhx.server.adaptive.algorithm.MAlgorithmInterface;
import com.septemberhx.server.base.MAnalyserInput;
import com.septemberhx.server.base.MAnalyserResult;
import com.septemberhx.server.base.MPlannerResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main part of the self-adaptive system.
 * It follows the standard system design: analyser, planner and executor
 */
public class MAdaptiveSystem {

    private static Logger logger = LogManager.getLogger(MAdaptiveSystem.class);
    private MAnalyser analyser;
    private MPlanner planner;
    private MExecutor executor;
    private MAlgorithmInterface algorithm;

    public MAdaptiveSystem() {
        this.analyser = new MAnalyser();
        this.planner = new MPlanner();
        this.executor = new MExecutor();
        this.algorithm = new MBaseAlgorithm();
    }

    public void doLoopOnce() {
        // todo: provide the necessary information for the decision maker
        MAnalyserInput analyserInput = new MAnalyserInput();

        logger.info(analyserInput);
        MAnalyserResult analyserOutput = this.analyser.analyse(analyserInput);

        logger.info(analyserOutput);
        MPlannerResult plannerOutput = this.planner.plan(analyserOutput, this.algorithm);

        logger.info(plannerOutput);
        this.executor.execute(plannerOutput);
    }
}

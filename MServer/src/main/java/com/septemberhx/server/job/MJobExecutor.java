package com.septemberhx.server.job;

import com.septemberhx.server.core.MJobManager;
import com.septemberhx.server.core.MServerSkeleton;
import com.septemberhx.server.utils.MServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class MJobExecutor {

    private static Logger logger = LogManager.getLogger(MJobExecutor.class);

    public static void doJob(MBaseJob job) {
        if (job == null) return;
        logger.info("Execute job " + job.getId());
        System.out.println("Execute job " + job.getId());
        switch (job.getType()) {
            case BUILD:
                MBuildJob buildJob = (MBuildJob) job;
                MServerUtils.sendBuildInfo(buildJob.toBuildInfoRequest());
                logger.info("Build job info send");
                break;
            case DEPLOY:
                MDeployJob deployJob = (MDeployJob) job;
                MServerUtils.sendDeployInfo(deployJob.toMDeployPodRequest());
                logger.info("Deploy job info send");
                break;
            case NOTIFY:
                break;
            case SPLIT:
                doJob(job.nextJob());
                break;
            default:
                break;
        }
    }

    public static void nextJob(String prevJobId) {
        MJobManager jobManager = MServerSkeleton.getInstance().getJobManager();
        Optional<MBaseJob> prevJob = jobManager.getById(prevJobId);
        prevJob.ifPresent(job -> {
            logger.info("Execute job " + job.getId() + " finished");
            job.setCompleted(true);
            Optional<MBaseJob> parentJobOp = jobManager.getById(job.getParentId());
            parentJobOp.ifPresent(parentJob -> {
                MBaseJob nextJob = parentJob.nextJob();
                if (nextJob != null) {
                    MJobExecutor.doJob(nextJob);
                } else {
                    logger.info("Execute job " + parentJob.getId() + " finished");
                    parentJob.setCompleted(true);
                }
            });
        });
    }
}

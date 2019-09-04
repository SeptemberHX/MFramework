package com.septemberhx.server.adaptive.executor;

import com.septemberhx.server.job.MBuildJob;
import com.septemberhx.server.job.MJobExecutor;

public class MBuildExecutor {

    public void executeBuildJob(MBuildJob buildJob) {
        MJobExecutor.doJob(buildJob);
    }
}

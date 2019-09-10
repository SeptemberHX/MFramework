package com.septemberhx.server.adaptive.executor;

import com.septemberhx.server.job.MDeployJob;
import com.septemberhx.server.job.MJobExecutor;

public class MClusterExecutor {

    public void executeDeployJob(MDeployJob deployJob) {
        MJobExecutor.doJob(deployJob);
    }
}

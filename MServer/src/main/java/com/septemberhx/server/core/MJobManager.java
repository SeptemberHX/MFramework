package com.septemberhx.server.core;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.job.MBaseJob;

public class MJobManager extends MObjectManager<MBaseJob> {
    public void addJob(MBaseJob job) {
        this.objectMap.put(job.getId(), job);
        for (MBaseJob subJob : job.getSubJobs()) {
            this.objectMap.put(subJob.getId(), subJob);
        }
    }
}

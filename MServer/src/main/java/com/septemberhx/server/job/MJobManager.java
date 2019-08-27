package com.septemberhx.server.job;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.job.MBaseJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MJobManager extends MObjectManager<MBaseJob> {

    Map<String, List<String>> jobResult = new HashMap<>();

    public void addJob(MBaseJob job) {
        this.objectMap.put(job.getId(), job);
        for (MBaseJob subJob : job.getSubJobs()) {
            this.objectMap.put(subJob.getId(), subJob);
        }
    }

    public void addResult(String jobId, String result) {
        if (!jobResult.containsKey(jobId)) {
            jobResult.put(jobId, new ArrayList<>());
        }
        jobResult.get(jobId).add(result);
    }
}

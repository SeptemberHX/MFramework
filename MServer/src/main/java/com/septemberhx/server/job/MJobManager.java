package com.septemberhx.server.job;

import com.septemberhx.common.base.MObjectManager;
import com.septemberhx.server.job.MBaseJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MJobManager extends MObjectManager<MBaseJob> {

    Map<String, List<String>> jobResult = new HashMap<>();

    public void reset() {
        this.objectMap.clear();
        this.jobResult.clear();
    }

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

    public List<MBaseJob> getNextJobList() {
        int firstPriority = -1;
        for (MBaseJob job : this.getAllValues()) {
            if (job.isDoing()) {
                return new ArrayList<>();
            }

            if (job.isPending() && (firstPriority == -1 || firstPriority > job.getPriority())) {
                firstPriority = job.getPriority();
            }
        }

        List<MBaseJob> baseJobs = new ArrayList<>();
        for (MBaseJob job : this.getAllValues()) {
            if (job.isPending() && job.getPriority() == firstPriority) {
                baseJobs.add(job);
            }
        }
        return baseJobs;
    }

    public boolean hasUnfinishedJob() {
        for (MBaseJob baseJob : this.getAllValues()) {
            if (baseJob.isPending()) {
                return true;
            }
        }
        return false;
    }
}

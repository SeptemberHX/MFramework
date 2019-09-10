package com.septemberhx.server.base;


import com.septemberhx.server.job.MBaseJob;
import com.septemberhx.server.job.MJobManager;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The output of the planner which will be fed into the Executor
 */
@Getter
@Setter
public class MPlannerResult {
    /*
      if the planner calculated for the evolution plan successfully.
        Yes means the executor will accept the plan and to the evolution;
        No means the analyzer should analyze the RESULT of the evolution and do the plan for the second time.
     */
    private boolean success;

    /*
      The job id lists. It should be in order so that the executor can execute jobs from begin to end.
     */
    private List<String> jobIdList;

    /*
      Job manager which is used to manage all jobs in the plan due to the relationship between jobs.
     */
    private MJobManager jobManager;

    public MPlannerResult() {
        this.jobIdList = new ArrayList<>();
        this.jobManager = new MJobManager();
    }

    /**
     * Add root job to the plan. Child jobs are not permitted
     * @param rootBaseJob: Root job
     */
    public void addJob(MBaseJob rootBaseJob) {
        if (rootBaseJob.getParentId() != null) {
            throw new RuntimeException("Cannot add Non-root job to MPlannerResult");
        }

        this.jobManager.addJob(rootBaseJob);
        this.jobIdList.add(rootBaseJob.getId());
    }

    public void addJobs(List<MBaseJob> jobList) {
        for (MBaseJob job : jobList) {
            this.addJob(job);
        }
    }
}

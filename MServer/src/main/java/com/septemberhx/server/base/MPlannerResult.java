package com.septemberhx.server.base;

import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.job.MBaseJob;
import lombok.Getter;
import lombok.Setter;

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
    private List<MBaseJob> jobList;
    private MServerOperator serverOperator;
}

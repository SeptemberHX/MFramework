package com.septemberhx.server.adaptive.executor;

import com.septemberhx.server.base.MPlannerResult;
import lombok.Getter;
import lombok.Setter;


/**
 * carry out the given plan
 */
@Getter
@Setter
public class MExecutor {
    private MClusterExecutor clusterExecutor;
    private MBuildExecutor buildExecutor;

    public MExecutor(MClusterExecutor clusterExecutor, MBuildExecutor buildExecutor) {
        this.clusterExecutor = clusterExecutor;
        this.buildExecutor = buildExecutor;
    }

    public void execute(MPlannerResult plannerOutput) {
        // todo: the logic
        return;
    }
}

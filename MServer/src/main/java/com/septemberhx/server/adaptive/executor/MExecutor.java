package com.septemberhx.server.adaptive.executor;

import com.septemberhx.common.base.MService;
import com.septemberhx.server.base.MPlannerResult;
import com.septemberhx.server.core.MServerSkeleton;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        List<MBaseJob> baseJobList = plannerOutput.getJobList();
        MServerSkeleton.getInstance().getJobManager().reset();
        for (MBaseJob baseJob : baseJobList) {
            System.out.println(baseJob.toString());
            // all the switch jobs will be in a MBigSwitch job.
            if (baseJob.getType() != MJobType.SWITCH) {
                MServerSkeleton.getInstance().getJobManager().addJob(baseJob);
            }
        }

        // delete job has the highest priority
        // the switch job has the lowest priority
        // For one service, if it's docker image doesn't exist or there is a build job about it,
        //    then the deploy job has lower priority than build job.
        //    Otherwise they has the same priority with delete job
        int currPriority = 1;
        Map<String, MBaseJob> serviceId2BuildJob = new HashMap<>();
        List<MBaseJob> switchJobList = new ArrayList<>();
        for (MBaseJob baseJob : baseJobList) {
            if (baseJob.getType() == MJobType.DELETE) {
                baseJob.setPriority(currPriority);
            }

            // for com-service build
            if (baseJob.getType() == MJobType.CBUILD) {
                baseJob.setPriority(currPriority);
                serviceId2BuildJob.put(((MCBuildJob) baseJob).getCompositionRequest().getName(), baseJob);
            }

            // for simple service build
            if (baseJob.getType() == MJobType.BUILD) {
                baseJob.setPriority(currPriority);
                serviceId2BuildJob.put(((MBuildJob) baseJob).getServiceName(), baseJob);
            }

            if (baseJob.getType() == MJobType.SWITCH) {
                switchJobList.add(baseJob);
            }
        }

        for (MBaseJob baseJob : baseJobList) {
            if (baseJob.getType() == MJobType.DEPLOY) {
                MDeployJob deployJob = (MDeployJob) baseJob;
                if (serviceId2BuildJob.containsKey(deployJob.getServiceName())) {
                    deployJob.setPriority(currPriority + 1);
                } else {
                    List<MService> serviceList = MSystemModel.getIns().getServiceManager().getAllServicesByServiceName(deployJob.getServiceName());
                    if (serviceList.size() > 0 && serviceList.get(0).getDockerImageUrl() != null) {
                        deployJob.setPriority(currPriority);
                    }
                }
            }
        }

        currPriority += 2;
        for (MBaseJob baseJob : baseJobList) {
            if (baseJob.getType() == MJobType.DELETE) {
                baseJob.setPriority(currPriority);
            }
        }

        ++currPriority;
        MBigSwitchJob bigSwitchJob = new MBigSwitchJob();
        bigSwitchJob.setPriority(currPriority + 2);
        bigSwitchJob.setSwitchJobList(switchJobList);
        MServerSkeleton.getInstance().getJobManager().addJob(bigSwitchJob);

        // assign demands to instance
        MSystemModel.getIns().setDemandStateManager(plannerOutput.getServerOperator().getDemandStateManager().shallowClone());
        MSystemModel.getIns().setOperator(plannerOutput.getServerOperator());
        MSystemModel.getIns().setServiceManager(plannerOutput.getServerOperator().getServiceManager().shallowClone());

        MJobExecutor.doNextJobs();

        return;
    }
}

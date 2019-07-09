package com.septemberhx.server.controller;

import com.septemberhx.common.bean.*;
import com.septemberhx.server.base.MServiceInstance;
import com.septemberhx.server.core.MServerSkeleton;
import com.septemberhx.server.core.MSnapshot;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.MBuildJob;
import com.septemberhx.server.job.MDeployJob;
import com.septemberhx.server.job.MJobExecutor;
import com.septemberhx.server.job.MSplitJob;
import com.septemberhx.server.utils.MServerUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@EnableAutoConfiguration
@RequestMapping("/mserver")
public class MServerController {

    @ResponseBody
    @RequestMapping(path = "/loadInstanceInfo", method = RequestMethod.POST)
    public void loadInstanceInfo(@RequestBody MInstanceInfoBean instanceInfo) {
        System.out.println(instanceInfo);

        MSystemModel systemModel = MSnapshot.getInstance().getSystemModel();
        systemModel.loadInstanceInfo(instanceInfo);
    }

    @ResponseBody
    @RequestMapping(path = "/allInstance", method = RequestMethod.GET)
    public List<MServiceInstance> getAllServiceInstance() {
        return MSnapshot.getInstance().getSystemModel().getAllServiceInstance();
    }

    @ResponseBody
    @RequestMapping(path = "/getInstanceInfos", method = RequestMethod.GET)
    public MInstanceInfoResponse getInstanceInfos() {
        MInstanceInfoResponse response = MServerUtils.fetchAllInstanceInfo();
        for (MInstanceInfoBean infoBean : response.getInfoBeanList()) {
            if (infoBean != null) {
                MServerSkeleton.getInstance().updateInstanceInfo(infoBean);
            }
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(path = "/remoteuri", method = RequestMethod.POST)
    public URI getRemoteUri(@RequestBody MGetRemoteUriRequest remoteUriRequest) {
        return MServerSkeleton.getInstance().getRemoteUri(remoteUriRequest);
    }

    @ResponseBody
    @RequestMapping(path = "/setRemoteUri", method = RequestMethod.POST)
    public void setRemoteUri(@RequestBody MSetRestInfoRequest restInfoRequest) {
        MServerSkeleton.getInstance().setRemoteUri(restInfoRequest);
    }

    @RequestMapping(path = "/notifyJob", method = RequestMethod.GET)
    public void jobNotify(@RequestParam("jobId") String buildJobId) {
        MJobExecutor.nextJob(buildJobId);
    }

    @RequestMapping(path = "/test", method = RequestMethod.GET)
    public void test() {
        MSplitJob testJob = new MSplitJob();

        // sub job 1: build sampleservice2
        MBuildJob buildJob = new MBuildJob();
        buildJob.setGitUrl("git@192.168.1.104:SeptemberHX/mframework.git");
        buildJob.setBranch("master");
        buildJob.setProjectName("MFramework");
        buildJob.setModuleName("sampleservice2");
        buildJob.setImageTag("v1.0.2");
        buildJob.setGitTag(null);
        testJob.addSubJob(buildJob);

        // sub job 2: deploy sampleservice2
        MDeployJob deployJob = new MDeployJob();
        deployJob.setImageName(buildJob.getImageFullName());
        deployJob.setNodeId("ices-104");
        deployJob.setPod(MServerUtils.readPodYaml("sampleservice2"));
        testJob.addSubJob(deployJob);

        MServerSkeleton.getInstance().getJobManager().addJob(testJob);
        MJobExecutor.doJob(testJob);
    }
}

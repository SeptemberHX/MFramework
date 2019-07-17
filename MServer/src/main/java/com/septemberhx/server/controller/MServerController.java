package com.septemberhx.server.controller;

import com.septemberhx.common.bean.*;
import com.septemberhx.server.base.MServiceInstance;
import com.septemberhx.server.core.MServerSkeleton;
import com.septemberhx.server.job.*;
import com.septemberhx.server.utils.MServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@EnableAutoConfiguration
@RequestMapping("/mserver")
public class MServerController {

    private static Logger logger = LogManager.getLogger(MServerController.class);

    @ResponseBody
    @RequestMapping(path = "/loadInstanceInfo", method = RequestMethod.POST)
    public void loadInstanceInfo(@RequestBody MInstanceInfoBean instanceInfo) {
        logger.info(instanceInfo);
        MServerSkeleton.getInstance().updateInstanceInfo(instanceInfo);
    }

    @ResponseBody
    @RequestMapping(path = "/allInstance", method = RequestMethod.GET)
    public List<MServiceInstance> getAllServiceInstance() {
        return MServerSkeleton.getInstance().getAllInstanceInfos();
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
        logger.info("Job " + buildJobId + " notify accepted");
        MJobExecutor.concludeWork(buildJobId, null);
        MJobExecutor.doNextJobs(buildJobId);
    }

    @RequestMapping(path = "/notifyDeployJob", method = RequestMethod.POST)
    public void deployJobNotify(@RequestBody MDeployNotifyRequest deployNotifyRequest) {
        MJobExecutor.concludeWork(deployNotifyRequest.getId(), new MDeployJobResult(deployNotifyRequest));
        MJobExecutor.doNextJobs(deployNotifyRequest.getId());
    }

    @RequestMapping(path = "/test", method = RequestMethod.POST)
    public void test(@RequestBody MSplitJob testJob) {
//        MSplitJob testJob = new MSplitJob();

        // sub job 1: build sampleservice2
        MBuildJob buildJob = new MBuildJob();
        buildJob.setGitUrl("git@192.168.1.104:SeptemberHX/mframework.git");
        buildJob.setBranch("master");
        buildJob.setProjectName("MFramework");
        buildJob.setModuleName("SampleService3");
        buildJob.setImageTag("v1.0.5");
        buildJob.setGitTag(null);
        testJob.addSubJob(buildJob);

        // sub job 2: deploy sampleservice2
        MDeployJob deployJob = new MDeployJob();
        deployJob.setImageName(buildJob.getImageFullName());
        deployJob.setNodeId("ices-104");
        deployJob.setPod(MServerUtils.readPodYaml("sampleservice3"));
        testJob.addSubJob(deployJob);

        testJob.addSubJob(new MNotifyJob());

        MServerSkeleton.getInstance().getJobManager().addJob(testJob);
        MJobExecutor.doJob(testJob);
    }

    @RequestMapping(path = "/test2", method = RequestMethod.POST)
    public void test2(@RequestBody MCompositionRequest compositionRequest) {
        MCompositionJob compositionJob = new MCompositionJob();
        compositionJob.setCompositionRequest(compositionRequest);

        MCBuildJob mcBuildJob = new MCBuildJob();
        compositionRequest.setId(mcBuildJob.getId());
        mcBuildJob.setCompositionRequest(compositionRequest);
        compositionJob.addSubJob(mcBuildJob);

        MDeployJob deployJob = new MDeployJob();
        deployJob.setImageName(mcBuildJob.getImageFullName());
        deployJob.setNodeId("ices-104");
        deployJob.setPod(MServerUtils.getCompositionYaml(compositionRequest.getName()));
        compositionJob.addSubJob(deployJob);
        MServerSkeleton.getInstance().getJobManager().addJob(compositionJob);

        MJobExecutor.doJob(compositionJob);
    }
}

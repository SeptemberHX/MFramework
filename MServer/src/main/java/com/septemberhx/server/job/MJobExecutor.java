package com.septemberhx.server.job;

import com.septemberhx.common.base.*;
import com.septemberhx.common.bean.MApiContinueRequest;
import com.septemberhx.common.bean.MApiSplitBean;
import com.septemberhx.common.bean.MS2CSetApiCStatus;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.core.MRepoManager;
import com.septemberhx.server.core.MServerSkeleton;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.utils.MServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MJobExecutor {

    private static Logger logger = LogManager.getLogger(MJobExecutor.class);

    public static void doJob(MBaseJob job) {
        if (job == null) return;
        logger.info("Execute job " + job.getId());
        System.out.println("Execute job " + job.getId());
        switch (job.getType()) {
            case BUILD:
                MBuildJob buildJob = (MBuildJob) job;
                MServerUtils.sendBuildInfo(buildJob.toBuildInfoRequest());
                buildJob.markAsDoing();
                logger.info("Build job info send");
                break;
            case CBUILD:
                MCBuildJob mcBuildJob = (MCBuildJob) job;
                MServerUtils.sendCBuildInfo(mcBuildJob.getCompositionRequest());
                mcBuildJob.markAsDoing();
                logger.info("CBuild job info send");
                break;
            case DEPLOY:
                MDeployJob deployJob = (MDeployJob) job;
                MJobExecutor.doDeployJob(deployJob);
                deployJob.markAsDoing();
                logger.info("Deploy job info send");
                break;
            case DELETE:
                MDeleteJob deleteJob = (MDeleteJob) job;
                MJobExecutor.doDeleteJob(deleteJob);
                deleteJob.markAsDone();
                break;
            case BIGSWITCH:
                MBigSwitchJob switchJob = (MBigSwitchJob) job;
                MJobExecutor.doBigSwitchJob(switchJob);
                switchJob.markAsDone();
                break;
            case NOTIFY:
                doNotifyJob((MNotifyJob) job);
                break;
            case SPLIT:
            case COMPOSITE:
                MBaseJob nextJob;
                while ((nextJob = job.nextJob()) != null) {
                    MJobExecutor.doJob(nextJob);
                }
                break;
            default:
                break;
        }
    }

    private static void doDeployJob(MDeployJob deployJob) {
        List<MService> services = MSystemModel.getIns().getServiceManager().getAllServicesByServiceName(deployJob.getServiceName());
        deployJob.setImageName(services.get(0).getDockerImageUrl());
        MServerNode targetNode = MSystemModel.getIns().getMSNManager().getById(deployJob.getNodeId()).get();
        MServerUtils.sendDeployInfo(deployJob.toMDeployPodRequest(), targetNode.getNodeType());
    }

    private static void doDeleteJob(MDeleteJob deleteJob) {
        MServerNode targetNode = MSystemModel.getIns().getMSNManager().getById(deleteJob.getNodeId()).get();
        MServerUtils.sendDeleteInfo(deleteJob, targetNode.getNodeType());
        deleteJob.markAsDone();
        logger.info("Delete job info send");
    }

    private static void doBigSwitchJob(MBigSwitchJob bigSwitchJob) {
        // Here, we need copy all the demand state from the operator to MServerModel
        MUpdateCacheBean updateCacheBean = new MUpdateCacheBean();
        Map<String, String> urlMap = new HashMap<>();
        for (MBaseJob baseJob : bigSwitchJob.getSwitchJobList()) {
            MSwitchJob switchJob = (MSwitchJob) baseJob;
            urlMap.put(switchJob.getUserDemandId(), MServerSkeleton.fetchRequestUrl(switchJob.getUserDemandId(), ServerNodeType.CLOUD));
        }
        updateCacheBean.setDemandId2Url(urlMap);
        MServerUtils.sendUpdateCache(updateCacheBean);
    }

    private static void doNotifyJob(MNotifyJob notifyJob) {
        MJobManager jobManager = MServerSkeleton.getInstance().getJobManager();
        MRepoManager repoManager = MServerSkeleton.getInstance().getRepoManager();
        Optional<MBaseJob> parentJobOp = jobManager.getById(notifyJob.getParentId());
        parentJobOp.ifPresent(parentJob -> {
            switch (parentJob.getType()) {
                case SPLIT:
                    MSplitJob splitJob = (MSplitJob) parentJob;
                    List<MClassFunctionPair> chainList = repoManager.breakApiChains(
                        repoManager.getApiInfo(splitJob.getBreakBody()),
                        splitJob.getBreakPoint()
                    );

                    Optional<MServiceInstance> instanceOp = MServerSkeleton.getInstance().getInstanceInfo(splitJob.getInstanceId());
                    instanceOp.ifPresent(instance -> {
                        List<MApiSplitBean> splitBeans = new ArrayList<>();
                        for (MClassFunctionPair cfPair : chainList) {
                            for (String oId : instance.getParentIdMap().keySet()) {
                                if (oId.startsWith(cfPair.getClassName()) && instance.getParentIdMap().get(oId).equals(splitJob.getParentMObjectId())) {
                                    MApiSplitBean splitBean = new MApiSplitBean();
                                    splitBean.setObjectId(oId);
                                    splitBean.setFunctionName(cfPair.getFunctionName());
                                    splitBean.setStatus(false);
                                    splitBeans.add(splitBean);
                                }
                            }
                        }
                        MApiContinueRequest apiContinueRequest = new MApiContinueRequest();
                        apiContinueRequest.setSplitBeans(splitBeans);
                        MS2CSetApiCStatus ms2CSetApiCStatus = new MS2CSetApiCStatus();
                        ms2CSetApiCStatus.setInstanceId(splitJob.getInstanceId());
                        ms2CSetApiCStatus.setApiContinueRequest(apiContinueRequest);
                        logger.debug(ms2CSetApiCStatus);

                        MServerUtils.sendSetApiCSInfo(ms2CSetApiCStatus);
                    });
                    notifyJob.markAsDone();
                    doNextJobs(notifyJob.getId());
                    break;
                default:
                    break;
            }
        });
    }

    public static void doNextJobs() {
        List<MBaseJob> baseJobs = MServerSkeleton.getInstance().getJobManager().getNextJobList();
        if (baseJobs.size() > 0) {
            for (MBaseJob baseJob : baseJobs) {
                MJobExecutor.doJob(baseJob);
            }
        } else {
            System.out.println("========> All Jobs finished <=======");
        }

        if (MServerSkeleton.getInstance().getJobManager().getNextJobList().size() > 0) {
            MJobExecutor.doNextJobs();
        }
    }

    public static void doNextJobs(String prevJobId) {
        MJobManager jobManager = MServerSkeleton.getInstance().getJobManager();
        Optional<MBaseJob> prevJob = jobManager.getById(prevJobId);
        prevJob.ifPresent(job -> {
            Optional<MBaseJob> parentJobOp = jobManager.getById(job.getParentId());
            parentJobOp.ifPresent(parentJob -> {
                MBaseJob nextJob = parentJob.nextJob();
                if (nextJob != null) {
                    while ((nextJob = parentJob.nextJob()) != null) {
                        MJobExecutor.doJob(nextJob);
                    }
                } else {
                    logger.info("Execute job " + parentJob.getId() + " finished");
                    parentJob.markAsDone();
                }
            });
        });
    }

    public static void concludeWork(String finishedJobId, MBaseJobResult jobResult) {
        MJobManager jobManager = MServerSkeleton.getInstance().getJobManager();
        Optional<MBaseJob> finishedJobOp = jobManager.getById(finishedJobId);
        finishedJobOp.ifPresent(finishedJob -> {
            logger.info("Execute job " + finishedJob.getId() + " finished");
            finishedJob.markAsDone();

            if (jobResult == null) return;

            Optional<MBaseJob> parentJobOp = jobManager.getById(finishedJob.getParentId());
            parentJobOp.ifPresent(parentJob -> {
                switch (parentJob.getType()) {
                    case SPLIT:
                        if (jobResult.getType() == MJobType.DEPLOY) {
                            MDeployJobResult deployJobResult = (MDeployJobResult) jobResult;
                            jobManager.addResult(parentJob.getId(), deployJobResult.getInstanceId());
                        }
                        break;
                    default:
                        break;
                }
            });
        });
    }
}

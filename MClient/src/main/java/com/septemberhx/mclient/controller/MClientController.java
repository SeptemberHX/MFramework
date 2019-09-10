package com.septemberhx.mclient.controller;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.EurekaClient;
import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.bean.MApiContinueRequest;
import com.septemberhx.common.bean.MApiSplitBean;
import com.septemberhx.common.bean.MClientInfoBean;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.mclient.core.MClientSkeleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Author: septemberhx
 * @Date: 2019-06-13
 * @Version 0.1
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/mclient")
public class MClientController {

    @Qualifier("eurekaApplicationInfoManager")
    @Autowired
    private ApplicationInfoManager aim;

    @ResponseBody
    @RequestMapping(path = "/getMObjectIdList", method = RequestMethod.GET)
    public List<String> getMObjectIdList() {
        return MClientSkeleton.getInstance().getMObjectIdList();
    }

    @Qualifier("eurekaClient")
    @Autowired
    private EurekaClient discoveryClient;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    /**
     * Do something for MClient App:
     *   * Register new metadata so we can identify whether it is a MClient app or not
     */
    @PostConstruct
    public void init() {
        Map<String, String> map = aim.getInfo().getMetadata();
        map.put(MClusterConfig.MCLUSTER_SERVICE_METADATA_NAME, MClusterConfig.MCLUSTER_SERVICE_METADATA_VALUE);
        MClientSkeleton.getInstance().setDiscoveryClient(this.discoveryClient);
        MClientSkeleton.getInstance().setRequestMappingHandlerMapping(this.handlerMapping);
    }

    @ResponseBody
    @RequestMapping(path = "/info", method = RequestMethod.GET)
    public MClientInfoBean getInfo() {
        MClientInfoBean infoBean = new MClientInfoBean();
        infoBean.setApiMap(MClientSkeleton.getInstance().getObjectId2ApiSet());
        infoBean.setParentIdMap(MClientSkeleton.getInstance().getParentIdMap());
        infoBean.setMObjectIdSet(new HashSet<>(MClientSkeleton.getInstance().getMObjectIdList()));
        return infoBean;
    }

    @RequestMapping(path = "/setRestInfo", method = RequestMethod.POST)
    public void setRestInfo(@RequestBody MInstanceRestInfoBean restInfoBean) {
        MClientSkeleton.getInstance().addRestInfo(restInfoBean);
    }

    @RequestMapping(path = "/setApiContinueStatus", method = RequestMethod.POST)
    public void setApiContinueStatus(@RequestBody MApiContinueRequest continueStatus) {
        for (MApiSplitBean splitBean : continueStatus.getSplitBeans()) {
            MClientSkeleton.getInstance().setApiContinueStatus(splitBean);
        }
    }

    @ResponseBody
    @RequestMapping(path = "/getRestInfoList", method = RequestMethod.GET)
    public List<MInstanceRestInfoBean> getRestInfoList() {
        return MClientSkeleton.getInstance().getRestInfoBeanList();
    }
}

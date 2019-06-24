package com.septemberhx.mclient.controller;

import com.netflix.appinfo.ApplicationInfoManager;
import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.bean.MClientInfoBean;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.mclient.core.MClientInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
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

    @Autowired
    private ApplicationInfoManager aim;

    @ResponseBody
    @RequestMapping(path = "/getMObjectIdList", method = RequestMethod.GET)
    public List<String> getMObjectIdList() {
        return MClientInstance.getInstance().getMObjectIdList();
    }

    /**
     * Do something for MClient App:
     *   * Register new metadata so we can identify whether it is a MClient app or not
     */
    @PostConstruct
    public void init() {
        Map<String, String> map = aim.getInfo().getMetadata();
        map.put(MClusterConfig.MCLUSTER_SERVICE_METADATA_NAME, MClusterConfig.MCLUSTER_SERVICE_METADATA_VALUE);
    }

    @ResponseBody
    @RequestMapping(path = "/info", method = RequestMethod.GET)
    public MClientInfoBean getInfo() {
        MClientInfoBean infoBean = new MClientInfoBean();
        infoBean.setApiMap(MClientInstance.getInstance().getObjectId2ApiSet());
        infoBean.setParentIdMap(MClientInstance.getInstance().getParentIdMap());
        return infoBean;
    }

    @RequestMapping(path = "/setRestInfo", method = RequestMethod.POST)
    public void setRestInfo(@RequestBody MInstanceRestInfoBean restInfoBean) {
        MClientInstance.getInstance().addRestInfo(restInfoBean);
    }

    @ResponseBody
    @RequestMapping(path = "/getRestInfoList", method = RequestMethod.GET)
    public List<MInstanceRestInfoBean> getRestInfoList() {
        return MClientInstance.getInstance().getRestInfoBeanList();
    }
}

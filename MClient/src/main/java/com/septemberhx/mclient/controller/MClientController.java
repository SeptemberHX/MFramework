package com.septemberhx.mclient.controller;

import com.septemberhx.common.bean.MClientInfoBean;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.mclient.core.MClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: septemberhx
 * @Date: 2019-06-13
 * @Version 0.1
 */
@RestController
@EnableAutoConfiguration
@RequestMapping("/mclient")
public class MClientController {

    @ResponseBody
    @RequestMapping(path = "/getMObjectIdList", method = RequestMethod.GET)
    public List<String> getMObjectIdList() {
        return MClient.getInstance().getMObjectIdList();
    }

    @ResponseBody
    @RequestMapping(path = "/info", method = RequestMethod.GET)
    public MClientInfoBean getInfo() {
        MClientInfoBean infoBean = new MClientInfoBean();
        infoBean.setApiMap(MClient.getInstance().getObjectId2ApiSet());
        infoBean.setParentIdMap(MClient.getInstance().getParentIdMap());
        return infoBean;
    }

    @RequestMapping(path = "/setRestInfo", method = RequestMethod.POST)
    public void setRestInfo(@RequestBody MInstanceRestInfoBean restInfoBean) {
        MClient.getInstance().addRestInfo(restInfoBean);
    }

    @ResponseBody
    @RequestMapping(path = "/getRestInfoList", method = RequestMethod.GET)
    public List<MInstanceRestInfoBean> getRestInfoList() {
        return MClient.getInstance().getRestInfoBeanList();
    }
}

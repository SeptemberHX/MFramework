package com.septemberhx.mclient.controller;

import com.septemberhx.common.bean.MInstanceApiMapResponse;
import com.septemberhx.common.bean.MInstanceParentIdMapResponse;
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
    @RequestMapping(path = "/getParentIdMap", method = RequestMethod.GET)
    public MInstanceParentIdMapResponse getParentIdMap() {
        MInstanceParentIdMapResponse parentIdMapBean = new MInstanceParentIdMapResponse();
        parentIdMapBean.setParentIdMap(MClient.getInstance().getParentIdMap());
        return parentIdMapBean;
    }

    @ResponseBody
    @RequestMapping(path = "/getApiMap", method = RequestMethod.GET)
    public MInstanceApiMapResponse getApiMap() {
        MInstanceApiMapResponse response = new MInstanceApiMapResponse();
        response.setApiMap(MClient.getInstance().getObjectId2ApiSet());
        return response;
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

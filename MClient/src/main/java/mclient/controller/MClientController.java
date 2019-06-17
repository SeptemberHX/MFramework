package mclient.controller;

import com.septemberhx.common.bean.MInstanceParentIdMapResponse;
import mclient.core.MClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
}

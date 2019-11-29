package com.septemberhx.mgateway.core;

import com.septemberhx.common.base.MClusterConfig;
import com.septemberhx.common.base.MResponse;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.common.bean.MUserRequestBean;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.mgateway.utils.MGatewayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/22
 */
public class MGatewayProcess {

    private static Logger logger = LogManager.getLogger(MGatewayProcess.class);

    public static MResponse doRequest(MUserRequestBean requestBean) {
        MUserDemand userDemand = requestBean.getUserDemand();
        MResponse data = requestBean.getData();
        if (!MGatewayCache.getInstance().isCached(userDemand.getId())) {
            MGatewayCache.getInstance().updateCacheFromServer(userDemand);
        }

        MResponse response = null;
        if (!MGatewayCache.getInstance().isCached(userDemand.getId())) {
            logger.warn("Failed to doRequest for demand " + userDemand.getId());
        } else {
            String url = MGatewayCache.getInstance().getUrl(userDemand.getId());

            if (url.equals(MClusterConfig.REQUEST_SHOULD_SEND_TO_CLOUD)) {
                response = MGatewayUtils.clusterAgentClient.doRequest(requestBean);
            } else {
                try {
                    URI uri = new URI(url);
                    Map<String, List<String>> customHeaderMap = new HashMap<>();
                    List<String> userIdHeaderValue = new ArrayList<>();
                    userIdHeaderValue.add(userDemand.getUserId());
                    customHeaderMap.put("userId", userIdHeaderValue);
                    response = MRequestUtils.sendRequest(uri, data, MResponse.class, RequestMethod.POST, customHeaderMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (response == null) {
            response = MResponse.failResponse();
        }

        return response;
    }
}

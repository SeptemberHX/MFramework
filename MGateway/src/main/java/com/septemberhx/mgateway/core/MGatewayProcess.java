package com.septemberhx.mgateway.core;

import com.septemberhx.common.base.MResponse;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.common.utils.MRequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/22
 */
public class MGatewayProcess {

    private static Logger logger = LogManager.getLogger(MGatewayProcess.class);

    public static MResponse doRequest(MUserDemand userDemand, MResponse data) {
        if (!MGatewayCache.getInstance().isCached(userDemand.getId())) {
            MGatewayCache.getInstance().updateCacheFromServer(userDemand);
        }

        MResponse response = null;
        if (!MGatewayCache.getInstance().isCached(userDemand.getId())) {
            logger.warn("Failed to doRequest for demand " + userDemand.getId());
        } else {
            try {
                String url = MGatewayCache.getInstance().getUrl(userDemand.getId());
                URI uri = new URI(url);
                response = MRequestUtils.sendRequest(uri, data, MResponse.class, RequestMethod.POST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (response == null) {
            response = new MResponse("Fail");
        }

        return response;
    }
}

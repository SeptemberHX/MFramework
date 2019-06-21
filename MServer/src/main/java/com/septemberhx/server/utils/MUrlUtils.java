package com.septemberhx.server.utils;

import com.septemberhx.server.base.MClusterConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;

public class MUrlUtils {

    private final static Logger logger = LogManager.getLogger(MUrlUtils.class);

    public static URI getMclusterFetchInstanceInfoUri() {
        URI uri = null;
        try {
            uri = new URI(
                    "http",
                    null,
                    MClusterConfig.getInstance().getMClusterHost(),
                    MClusterConfig.getInstance().getMClusterPort(),
                    MClusterConfig.MCLUSTER_FETCH_INSTANCE_INFO, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        logger.debug(uri);
        return uri;
    }
}

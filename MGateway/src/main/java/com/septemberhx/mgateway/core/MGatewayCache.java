package com.septemberhx.mgateway.core;

import com.septemberhx.common.base.MUser;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.mgateway.client.MClusterAgentClient;
import com.septemberhx.mgateway.utils.MGatewayUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
 *
 * This class is used to store the user demand to instance ip mapping.
 * If it is not in the cache, it will ask the MServer.
 *
 * During the executing of an evolution plan, the cache will be updated by the MServer
 */
@Component
public class MGatewayCache {

    private static volatile MGatewayCache instance;
    private Map<String, MUser> userMap;  // every user use the gateway should store their information here
    private Map<String, String> demandId2Url;  // demand id to url

    public void clear() {
        this.userMap.clear();
        this.demandId2Url.clear();
    }

    private MGatewayCache() {
        this.userMap = new HashMap<>();
        this.demandId2Url = new HashMap<>();
    }

    public synchronized static MGatewayCache getInstance() {
        if (instance == null) {
            synchronized (MGatewayCache.class) {
                if (instance == null) {
                    instance = new MGatewayCache();
                }
            }
        }
        return instance;
    }

    public void addUser(MUser user) {
        this.userMap.put(user.getId(), user);
    }

    public List<MUser> getAllUser() {
        return new ArrayList<>(this.userMap.values());
    }

    public boolean isCached(String demandId) {
        return demandId2Url.containsKey(demandId);
    }

    public void updateCacheFromServer(MUserDemand userDemand) {
        String url = MGatewayUtils.clusterAgentClient.fetchRequestUrl(userDemand);
        if (url != null) {
            this.demandId2Url.put(userDemand.getId(), url);
        } else {
            this.demandId2Url.remove(userDemand.getId());
        }
    }

    public String getUrl(String demandId) {
        return this.demandId2Url.get(demandId);
    }

    public void update(String demandId, String url) {
        this.demandId2Url.put(demandId, url);
    }
}
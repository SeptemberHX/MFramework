package com.septemberhx.mclient.core;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.septemberhx.common.bean.MApiSplitBean;
import com.septemberhx.common.bean.MGetRemoteUriRequest;
import com.septemberhx.common.bean.MInstanceRestInfoBean;
import com.septemberhx.common.log.MBaseLog;
import com.septemberhx.common.log.MFunctionCallEndLog;
import com.septemberhx.common.log.MFunctionCalledLog;
import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.common.utils.MLogUtils;
import com.septemberhx.common.utils.MRequestUtils;
import com.septemberhx.common.utils.MUrlUtils;
import com.septemberhx.mclient.base.MObject;
import com.septemberhx.mclient.utils.RequestUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;

/**
 * @Author: septemberhx
 * @Date: 2019-06-12
 * @Version 0.1
 */
@Service
public class MClientSkeleton {

    private static volatile MClientSkeleton instance;
    @Getter
    private Map<String, MObject> mObjectMap;
    @Getter
    private Map<String, String> parentIdMap;
    @Getter
    private Map<String, Set<String>> objectId2ApiSet;

    private Map<String, Map<String, MInstanceRestInfoBean>> restInfoMap;
    private Map<String, Map<String, Boolean>> apiContinueMap;
    private static Logger logger = LogManager.getLogger(MClientSkeleton.class);

    @Setter
    private EurekaClient discoveryClient;

    @Setter
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private MClientSkeleton() {
        this.mObjectMap = new HashMap<>();
        this.parentIdMap = new HashMap<>();
        this.objectId2ApiSet = new HashMap<>();
        this.restInfoMap = new HashMap<>();
        this.apiContinueMap = new HashMap<>();
    }

    public static MClientSkeleton getInstance() {
        if (instance == null) {
            synchronized (MClientSkeleton.class) {
                if (instance == null) {
                    instance = new MClientSkeleton();
                }
            }
        }
        return instance;
    }

    /*
     * register object
     */
    public void registerMObject(MObject object) {
        if (this.mObjectMap.containsKey(object.getId())) {
            logger.warn("MObject " + object.getId() + " has been registered before !!!");
        } else {
            this.mObjectMap.put(object.getId(), object);
        }
    }

    /*
     * register the parent id of object
     */
    public void registerParent(MObject object, String parentId) {
        if (this.mObjectMap.containsKey(object.getId())) {
            this.parentIdMap.put(object.getId(), parentId);
        } else {
            logger.warn("MObject " + object.getId() + " not registered");
        }
    }

    public void printParentIdMap() {
        logger.debug(this.parentIdMap.toString());
    }

    public List<String> getMObjectIdList() {
        return new ArrayList<>(this.mObjectMap.keySet());
    }

    /*
     * add an info bean
     */
    public void addRestInfo(MInstanceRestInfoBean infoBean) {
        if (infoBean.getRestAddress() == null) {
            this.removeRestInfo(infoBean);
            return;
        }

        if (!this.restInfoMap.containsKey(infoBean.getObjectId())) {
            this.restInfoMap.put(infoBean.getObjectId(), new HashMap<>());
        }
        this.restInfoMap.get(infoBean.getObjectId()).put(infoBean.getFunctionName(), infoBean);
    }

    /*
     * delete an info bean
     */
    private void removeRestInfo(MInstanceRestInfoBean infoBean) {
        if (this.restInfoMap.containsKey(infoBean.getObjectId())) {
            this.restInfoMap.get(infoBean.getObjectId()).remove(infoBean.getFunctionName());
        }
    }

    /**
     * Get all Rest info
     * @return List
     */
    public List<MInstanceRestInfoBean> getRestInfoBeanList() {
        List<MInstanceRestInfoBean> restInfoBeans = new ArrayList<>();
        for (String mObjectId : this.restInfoMap.keySet()) {
            restInfoBeans.addAll(this.restInfoMap.get(mObjectId).values());
        }
        return restInfoBeans;
    }

    /**
     * It will be used by MApiType annotation
     * @param mObjectId: the id of MObject
     * @param functionName: the function will be used/called
     * @return boolean
     */
    public static boolean isRestNeeded(String mObjectId, String functionName) {
        return MClientSkeleton.getInstance().checkIfHasRestInfo(mObjectId, functionName);
    }

    public void setApiContinueStatus(MApiSplitBean apiSplitBean) {
        if (!this.apiContinueMap.containsKey(apiSplitBean.getObjectId())) {
            this.apiContinueMap.put(apiSplitBean.getObjectId(), new HashMap<>());
        }
        this.apiContinueMap.get(apiSplitBean.getObjectId()).put(apiSplitBean.getFunctionName(), apiSplitBean.getStatus());
    }

    public static boolean checkIfContinue(String mObjectId, String functionName) {
        if (!MClientSkeleton.getInstance().apiContinueMap.containsKey(mObjectId)) return true;
        return MClientSkeleton.getInstance().apiContinueMap.get(mObjectId).getOrDefault(functionName, true);
    }

    public static void logFunctionCall(String mObjectId, String functionName, HttpServletRequest request) {
        MFunctionCalledLog serviceBaseLog = new MFunctionCalledLog();
        serviceBaseLog.setLogDateTime(DateTime.now());
        serviceBaseLog.setLogMethodName(functionName);
        serviceBaseLog.setLogObjectId(mObjectId);
        serviceBaseLog.setLogFromIpAddr(request.getRemoteAddr());
        serviceBaseLog.setLogFromPort(request.getRemotePort());
        serviceBaseLog.setLogUserId(request.getHeader("userId"));
        // todo: get the ip address and set it to serviceBaseLog.logIpAddr
        MLogUtils.log(serviceBaseLog);
    }

    public static void logFunctionCallEnd(String mObjectId, String functionName, HttpServletRequest request) {
        MFunctionCallEndLog serviceBaseLog = new MFunctionCallEndLog();
        serviceBaseLog.setLogDateTime(DateTime.now());
        serviceBaseLog.setLogMethodName(functionName);
        serviceBaseLog.setLogObjectId(mObjectId);
        serviceBaseLog.setLogFromIpAddr(request.getRemoteAddr());
        serviceBaseLog.setLogFromPort(request.getRemotePort());
        serviceBaseLog.setLogUserId(request.getHeader("userId"));
        // todo: get the ip address and set it to serviceBaseLog.logIpAddr
        MLogUtils.log(serviceBaseLog);
    }

    /**
     * It will be used by MApiType annotation
     * @param mObjectId: the id of MObject
     * @param functionName: the function will be used/called
     * @param args: the arguments
     * @return Object
     */
    public static Object restRequest(String mObjectId, String functionName, String returnTypeStr, Object... args) {
        List<String> paramNameList = new ArrayList<>(args.length / 2);
        List<Object> paramValueList = new ArrayList<>(args.length / 2);
        for (int i = 0; i < args.length; i += 2) {
            paramNameList.add((String)args[i]);
            paramValueList.add(args[i+1]);
        }
        String paramJsonStr = RequestUtils.methodParamToJsonString(paramNameList, paramValueList);

        if (MClientSkeleton.getInstance().discoveryClient != null) {
            Application clusterAgent = MClientSkeleton.getInstance().discoveryClient.getApplication("MClusterAgent");
            if (clusterAgent != null) {
                List<InstanceInfo> clusterAgentInstances = clusterAgent.getInstances();
                if (clusterAgentInstances.size() > 0) {
                    // request MClusterAgent for remote uri
                    URI requestUri = MUrlUtils.getMClientRequestRemoteUri(clusterAgentInstances.get(0).getIPAddr(), clusterAgentInstances.get(0).getPort());
                    logger.debug(requestUri);
                    if (requestUri != null) {
                        String rawPatterns = null;
                        Map<RequestMappingInfo, HandlerMethod> mapping = MClientSkeleton.getInstance().requestMappingHandlerMapping.getHandlerMethods();
                        for (RequestMappingInfo mappingInfo : mapping.keySet()) {
                            if (mapping.get(mappingInfo).getMethod().getName().equals(functionName)) {
                                rawPatterns = mappingInfo.getPatternsCondition().toString();
                                break;
                            }
                        }
                        MGetRemoteUriRequest getRemoteUriRequest = new MGetRemoteUriRequest();
                        getRemoteUriRequest.setFunctionName(functionName);
                        getRemoteUriRequest.setObjectId(mObjectId);
                        getRemoteUriRequest.setRawPatterns(rawPatterns);
                        URI remoteUri = MRequestUtils.sendRequest(requestUri, getRemoteUriRequest, URI.class, RequestMethod.POST);
                        logger.debug(remoteUri);
                        if (remoteUri != null) {
                            // redirect to remote uri with parameters in json style
                            try {
                                return MRequestUtils.sendRequest(remoteUri, JSONObject.stringToValue(paramJsonStr), Class.forName(returnTypeStr), RequestMethod.GET);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * request the information that needed by rest request for remote call
     * @param mObjectId: the id of MObject
     * @param functionName: the function will be used/called
     * @return String
     */
    public String getRestInfo(String mObjectId, String functionName) {
        if (!this.checkIfHasRestInfo(mObjectId, functionName)) {
            throw new RuntimeException("Failed to fetch remote url for " + functionName + " in " + mObjectId);
        }
        return this.restInfoMap.get(mObjectId).get(functionName).getRestAddress();
    }

    /**
     * check whether need to use remote call or not
     * @param mObjectId: the id of MObject
     * @param functionName: the function will be used/called
     * @return boolean
     */
    private boolean checkIfHasRestInfo(String mObjectId, String functionName) {
        return this.restInfoMap.containsKey(mObjectId) && this.restInfoMap.get(mObjectId).containsKey(functionName);
    }

    public void registerObjectAndApi(String mObjectId, String apiName) {
        if (!this.objectId2ApiSet.containsKey(mObjectId)) {
            this.objectId2ApiSet.put(mObjectId, new HashSet<>());
        }
        this.objectId2ApiSet.get(mObjectId).add(apiName);
    }
}

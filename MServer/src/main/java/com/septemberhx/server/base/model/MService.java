package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/12
 */
@Getter
public class MService extends MBaseObject {
    private String serviceName;                     // Service Name
    private String gitUrl;                          // the git repo url
    private Map<String, MServiceInterface> interfaceMap;  // interface list

    public MService(String id, String name, String gitUrl, Map<String, MServiceInterface> interfaceMap) {
        this.id = id;
        this.serviceName = name;
        this.gitUrl = gitUrl;
        this.interfaceMap = interfaceMap;
    }

    public List<MServiceInterface> getInterfaceMetUserDemand(MUserDemand userDemand) {
        List<MServiceInterface> resultList = new ArrayList<>();
        for (MServiceInterface serviceInterface : this.interfaceMap.values()) {
            if (userDemand.isServiceInterfaceMet(serviceInterface)) {
                resultList.add(serviceInterface);
            }
        }
        return resultList;
    }

    public static Boolean checkIfInstanceIsGatewayByServiceName(String serviceName) {
        return serviceName.startsWith("Gateway");
    }
}

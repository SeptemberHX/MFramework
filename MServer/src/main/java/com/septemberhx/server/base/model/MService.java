package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/12
 */
public class MService extends MBaseObject {
    private String serviceName;                     // Service Name
    private String gitUrl;                          // the git repo url
    private List<MServiceInterface> interfaceList;  // interface list

    public MService(String id, String name, String gitUrl, List<MServiceInterface> interfaces) {
        this.id = id;
        this.serviceName = name;
        this.gitUrl = gitUrl;
        this.interfaceList = interfaces;
    }

    public List<MServiceInterface> getInterfaceMetUserDemand(MUserDemand userDemand) {
        List<MServiceInterface> resultList = new ArrayList<>();
        for (MServiceInterface serviceInterface : this.interfaceList) {
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

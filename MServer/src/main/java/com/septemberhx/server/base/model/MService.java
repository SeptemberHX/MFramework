package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private MResource resource;
    private Integer maxUserCap;

    public MService(String id, String name, String gitUrl, Map<String, MServiceInterface> interfaceMap) {
        this.id = id;
        this.serviceName = name;
        this.gitUrl = gitUrl;
        this.interfaceMap = interfaceMap;
        this.resource = new MResource();
        this.maxUserCap = 0;
    }

    public List<MServiceInterface> getInterfaceMetUserDemand(MUserDemand userDemand) {
        return this.interfaceMap.values().stream().filter(userDemand::isServiceInterfaceMet)
                .collect(Collectors.toList());
    }

    public static Boolean checkIfInstanceIsGatewayByServiceName(String serviceName) {
        return serviceName.startsWith("Gateway");
    }

    public List<MServiceInterface> getAllInterface() {
        return new ArrayList<>(this.interfaceMap.values());
    }

    public boolean checkIfMeetDemand(MUserDemand userDemand) {
        return !this.getInterfaceMetUserDemand(userDemand).isEmpty();
    }
}

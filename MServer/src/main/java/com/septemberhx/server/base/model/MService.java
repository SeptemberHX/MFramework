package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import com.septemberhx.common.base.MArchitectInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/12
 */
@Setter
@Getter
public class MService extends MBaseObject {
    private boolean generated;
    private String serviceName;                     // Service Name, it should be unique
    private String gitUrl;                          // the git repo url
    private Map<String, MServiceInterface> interfaceMap;  // interface list
    private MResource resource;
    private Integer maxUserCap;
    private MArchitectInfo artifactInfo;
    private String rId;                             // same service will have different rId if require different resources
                                                    // and it should be ordered. Small stands for low resources, low sla
                                                    // Big stands for high resources, high sla

    public MService(String id, String name, String gitUrl, Map<String, MServiceInterface> interfaceMap) {
        this.generated = false;
        this.id = id;
        this.serviceName = name;
        this.gitUrl = gitUrl;
        this.interfaceMap = interfaceMap;
        this.resource = new MResource();
        this.maxUserCap = 0;
        this.artifactInfo = new MArchitectInfo();
    }

    public void verify() {
        for (String interfaceId : this.interfaceMap.keySet()) {
            MServiceInterface serviceInterface = this.interfaceMap.get(interfaceId);

            if (!serviceInterface.getServiceId().equals(this.id)) {
                throw new RuntimeException(
                        String.format("%s|%s failed to be verified", this.id, serviceInterface.getInterfaceId())
                );
            }

            if (!serviceInterface.getInterfaceId().equals(interfaceId)) {
                throw new RuntimeException(
                        String.format("%s|%s failed to be verified", this.id, serviceInterface.getInterfaceId())
                );
            }
        }
    }

    public List<MServiceInterface> getInterfaceMetUserDemand(MUserDemand userDemand) {
        return this.interfaceMap.values().stream().filter(userDemand::isServiceInterfaceMet)
                .collect(Collectors.toList());
    }

    public static boolean checkIfInstanceIsGatewayByServiceName(String serviceName) {
        return serviceName.startsWith("Gateway");
    }

    public List<MServiceInterface> getAllInterface() {
        return new ArrayList<>(this.interfaceMap.values());
    }

    public boolean checkIfMeetDemand(MUserDemand userDemand) {
        return !this.getInterfaceMetUserDemand(userDemand).isEmpty();
    }

    public List<MServiceInterface> getAllComInterfaces() {
        return this.interfaceMap.values().stream().filter(MServiceInterface::isGenerated).collect(Collectors.toList());
    }

    public MServiceInterface getInterfaceById(String interfaceId) {
        return this.interfaceMap.get(interfaceId);
    }

    @Override
    public String toString() {
        return "MService{" +
                "generated=" + generated +
                ", serviceName='" + serviceName + '\'' +
                ", gitUrl='" + gitUrl + '\'' +
                ", interfaceMap=" + interfaceMap +
                ", resource=" + resource +
                ", maxUserCap=" + maxUserCap +
                ", artifactInfo=" + artifactInfo +
                ", id='" + id + '\'' +
                ", rId='" + rId + '\'' +
                '}';
    }
}

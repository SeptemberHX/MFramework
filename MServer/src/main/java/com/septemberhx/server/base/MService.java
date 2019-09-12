package com.septemberhx.server.base;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/12
 */
public class MService {
    private String serviceId;                       // Unique Id for service
    private String serviceName;                     // Service Name
    private String gitUrl;                          // the git repo url
    private List<MServiceInterface> interfaceList;  // interface list

    public List<MServiceInterface> getInterfaceMetUserDemand(MUserDemand userDemand) {
        List<MServiceInterface> resultList = new ArrayList<>();
        for (MServiceInterface serviceInterface : this.interfaceList) {
            if (userDemand.isServiceInterfaceMet(serviceInterface)) {
                resultList.add(serviceInterface);
            }
        }
        return resultList;
    }
}

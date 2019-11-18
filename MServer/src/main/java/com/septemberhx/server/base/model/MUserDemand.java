package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/11
 */
@Getter
@Setter
public class MUserDemand extends MBaseObject {
    private String userId;      // user id
    private String functionId;  // unique ID for functions. It will be used when try to map demands to services
    private Integer slaLevel;   // the SLA level that users expect for
    private String serviceName;

    public MUserDemand(String userId, String functionId, Integer slaLevel, String serviceName) {
        this.userId = userId;
        this.functionId = functionId;
        this.slaLevel = slaLevel;
        this.id = userId + "_" + functionId + "_" + slaLevel;
        this.serviceName = serviceName;
    }

    public boolean isDemandMet(String functionIdProvided, Integer slaLevelProvided) {
        return functionIdProvided.equals(functionId) && slaLevelProvided >= slaLevel;
    }

    public boolean isServiceInterfaceMet(MServiceInterface serviceInterface) {
        if (this.serviceName != null && !serviceInterface.getServiceId().startsWith(this.serviceName)) return false;
        return this.isDemandMet(serviceInterface.getFunctionId(), serviceInterface.getSlaLevel());
    }

    @Override
    public String toString() {
        return "MUserDemand{" +
                "userId='" + userId + '\'' +
                ", functionId='" + functionId + '\'' +
                ", slaLevel=" + slaLevel +
                ", serviceId='" + serviceName + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}

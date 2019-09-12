package com.septemberhx.server.base;

import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/11
 */
@Getter
@Setter
public class MUserDemand {
    private String userId;      // user id
    private String functionId;  // unique ID for functions. It will be used when try to map demands to services
    private Integer slaLevel;   // the SLA level that users expect for

    public boolean isDemandMet(String functionIdProvided, Integer slaLevelProvided) {
        return functionIdProvided.equals(functionId) && slaLevelProvided >= slaLevel;
    }

    public boolean isServiceInterfaceMet(MServiceInterface serviceInterface) {
        return this.isDemandMet(serviceInterface.getFunctionId(), serviceInterface.getSlaLevel());
    }
}

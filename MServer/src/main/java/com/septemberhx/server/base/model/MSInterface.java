package com.septemberhx.server.base.model;

import lombok.Getter;

import java.util.Objects;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/22
 *
 * Interface of a service
 * Short version of MServiceInterface
 */
@Getter
public class MSInterface {

    private String interfaceName;   // the interface name
    private String serviceId;

    public MSInterface(String interfaceName, String serviceId) {
        this.serviceId = serviceId;
        this.interfaceName = interfaceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MSInterface that = (MSInterface) o;
        return Objects.equals(interfaceName, that.interfaceName) &&
                Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaceName, serviceId);
    }
}

package com.septemberhx.server.base.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/23
 */
@Getter
@Setter
public class MResource {
    private Long cpu;
    private Long ram;
    private Long bandwidth;

    public MResource() {
        this.cpu = 0L;
        this.ram = 0L;
        this.bandwidth = 0L;
    }

    public MResource(Long cpu, Long ram, Long bandwidth) {
        this.cpu = cpu;
        this.ram = ram;
        this.bandwidth = bandwidth;
    }

    public boolean isEnough(MResource mResource) {
        return this.cpu >= mResource.cpu
                && this.ram >= mResource.ram
                && this.bandwidth >= mResource.bandwidth;
    }

    public void assign(MResource mResource) {
        this.cpu = this.cpu - mResource.cpu;
        this.ram = this.ram - mResource.ram;
        this.bandwidth = this.bandwidth - mResource.bandwidth;
    }

    public void free(MResource mResource) {
        this.cpu = this.cpu + mResource.cpu;
        this.ram = this.ram + mResource.ram;
        this.bandwidth = this.bandwidth + this.bandwidth;
    }

    public MResource sub(MResource mResource) {
        return new MResource(
            this.cpu - mResource.cpu,
            this.ram - mResource.ram,
            this.bandwidth - mResource.bandwidth
        );
    }

    public MResource add(MResource mResource) {
        return new MResource(
            this.cpu + mResource.cpu,
            this.ram + mResource.ram,
            this.bandwidth + mResource.bandwidth
        );
    }

    @Override
    public String toString() {
        return "MResource{" +
                "cpu=" + cpu +
                ", ram=" + ram +
                ", bandwidth=" + bandwidth +
                '}';
    }
}

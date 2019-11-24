package com.septemberhx.common.base;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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

    public MResource(MResource o) {
        this.cpu = o.cpu;
        this.ram = o.ram;
        this.bandwidth = o.bandwidth;
    }

    public MResource deepClone() {
        return new MResource(this);
    }

    public boolean isEnough(MResource mResource) {
        return this.cpu >= mResource.cpu
                && this.ram >= mResource.ram;
    }

    public void assign(MResource mResource) {
        this.cpu = this.cpu - mResource.cpu;
        this.ram = this.ram - mResource.ram;
    }

    public void free(MResource mResource) {
        this.cpu = this.cpu + mResource.cpu;
        this.ram = this.ram + mResource.ram;
    }

    public MResource sub(MResource mResource) {
        return new MResource(
            this.cpu - mResource.cpu,
            this.ram - mResource.ram,
            this.bandwidth
        );
    }

    public MResource add(MResource mResource) {
        return new MResource(
            this.cpu + mResource.cpu,
            this.ram + mResource.ram,
            this.bandwidth
        );
    }

    public MResource max(MResource mResource) {
        return new MResource(
                Math.max(this.cpu, mResource.cpu),
                Math.max(this.ram, mResource.ram),
                Math.max(this.bandwidth, mResource.bandwidth)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MResource resource = (MResource) o;
        return Objects.equals(cpu, resource.cpu) &&
                Objects.equals(ram, resource.ram) &&
                Objects.equals(bandwidth, resource.bandwidth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpu, ram, bandwidth);
    }
}

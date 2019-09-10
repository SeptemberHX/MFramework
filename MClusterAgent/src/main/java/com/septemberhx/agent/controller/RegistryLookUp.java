package com.septemberhx.agent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RegistryLookUp {

    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/lookup/{serviceId}")
    public List<ServiceInstance> lookup(@PathVariable String serviceId) {
        return discoveryClient.getInstances(serviceId);
    }

}
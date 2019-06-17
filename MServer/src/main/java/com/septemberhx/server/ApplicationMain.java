package com.septemberhx.server;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;

public class ApplicationMain {
    public static void main(String[] args) throws Exception {
        ApiClient client = Config.fromUrl("http://192.168.1.102:8082");
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName() + " " + item.getStatus().getPodIP());
            for (V1Container container : item.getSpec().getContainers()) {
                System.out.println(container.getPorts());
            }
        }

        V1NodeList nodeList = api.listNode(false, null, null, null, null, null, null, null, null);
        for (V1Node item : nodeList.getItems()) {
            System.out.println(item.getMetadata().getName());
        }
    }
}

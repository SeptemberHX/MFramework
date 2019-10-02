package com.septemberhx.server.adaptive.algorithm;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.septemberhx.common.base.MClassFunctionPair;
import com.septemberhx.common.base.MArchitectInfo;
import com.septemberhx.common.bean.MCompositionRequest;
import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.base.model.MSIInterface;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInterface;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.MCompositionJob;
import com.septemberhx.server.utils.MIDUtils;

import java.util.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/29
 *
 * This class is used to detect the potential services that can be composed or split.
 * It should be executed before minor/major algorithm
 */
public class MCompositionAlgorithmInCommon {
    public static void doCompositionPart(MutableValueGraph<MSIInterface, Integer> interfaceGraph) {
        List<EndpointPair<MSIInterface>> edgeList = new ArrayList<>(interfaceGraph.edges());
        Collections.sort(edgeList, (o1, o2) ->
                -interfaceGraph.edgeValueOrDefault(o1, 0).compareTo(interfaceGraph.edgeValueOrDefault(o2, 0)));
        Integer allCallCount = 0;
        for (EndpointPair<MSIInterface> edge : edgeList) {
            allCallCount += interfaceGraph.edgeValueOrDefault(edge, 0);
        }

        List<EndpointPair<MSIInterface>> resultList = new ArrayList<>();
        for (EndpointPair<MSIInterface> edge : edgeList) {
            if (interfaceGraph.edgeValueOrDefault(edge, 0) > allCallCount * MAdaptiveSystem.COMPOSITION_THRESHOLD) {
                resultList.add(edge);
            } else {
                break;
            }
        }
    }

    public static MService compositeService(MService service1, MServiceInterface interface1, MService service2, MServiceInterface interface2) {
        // interfaceId contains serviceName
        MServerOperator operator = MSystemModel.getIns().getOperator();
        String serviceId = MIDUtils.generateServiceId(String.format("%s__%s", interface1.getInterfaceId(), interface2.getInterfaceId()));
        String serviceName = serviceId;
        String functionName = String.format("%s__%s", interface1.getFullFuncName(), interface2.getFullFuncName());
        MServiceInterface newInterface = new MServiceInterface();
        newInterface.setInterfaceId(MIDUtils.generateInterfaceId(serviceId, functionName));
        newInterface.setSlaLevel(-1);
        newInterface.setFunctionId(MIDUtils.generateFunctionId(functionName));
        newInterface.setServiceId(serviceId);

        List<String> compositionList = new ArrayList<>();
        compositionList.addAll(interface1.getCompositionList());
        compositionList.addAll(interface2.getCompositionList());
        newInterface.setCompositionList(compositionList);

        Map<String, MServiceInterface> interfaceMap = new HashMap<>();
        interfaceMap.put(newInterface.getInterfaceId(), newInterface);

        MService newService = new MService(serviceId, serviceName, null, interfaceMap);
        newService.setGenerated(true);
        newService.setMaxUserCap(Math.min(service1.getMaxUserCap(), service2.getMaxUserCap()));
        newService.setResource(service1.getResource().max(service2.getResource()));
        return newService;
    }

    /**
     * Generate a new MCompositionJob according to given new generated composited service
     * @param composedService
     * @return
     */
    public static MCompositionJob compositeJob(MService compositedService) {
        MServerOperator operator = MSystemModel.getIns().getOperator();
        MCompositionJob compositionJob = new MCompositionJob();
        MCompositionRequest compositionRequest = new MCompositionRequest();

        String requestId = "";      // generated, random is ok
        String serviceName = compositedService.getServiceName();
        String docker_owner = "";   // should be owned by system admin
        String docker_tag = "";     // version tag
        String docker_name = compositedService.getServiceName();
        String register_url = "";   // should be the register url of the cluster that wants to use this

        List<MClassFunctionPair> classFunctionPairs = operator.getCallChainList(compositedService);    // build with the info in service repo
        List<MArchitectInfo> dependencies = new ArrayList<>();     // build with the info in service repo


        compositionRequest.setId(requestId);
        compositionRequest.setName(serviceName);
        compositionRequest.setDocker_owner(docker_owner);
        compositionRequest.setDocker_tag(docker_tag);
        compositionRequest.setDocker_name(docker_name);
        compositionRequest.setRegister_url(register_url);
        compositionRequest.setChain_list(classFunctionPairs);
        compositionRequest.setDependencies(dependencies);

        return compositionJob;
    }
}

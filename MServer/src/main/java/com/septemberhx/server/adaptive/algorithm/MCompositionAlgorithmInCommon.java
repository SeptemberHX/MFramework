package com.septemberhx.server.adaptive.algorithm;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.base.model.MSIInterface;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MServiceInterface;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
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
        String functionName = String.format("%s__%s", interface1.getFunctionName(), interface2.getFunctionName());
        MServiceInterface newInterface = new MServiceInterface();
        newInterface.setInterfaceId(MIDUtils.generateInterfaceId(serviceId, functionName));
        newInterface.setSlaLevel(-1);
        newInterface.setFunctionId(MIDUtils.generateFunctionId(functionName));

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
}

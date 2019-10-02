package com.septemberhx.server.adaptive.algorithm;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.septemberhx.common.base.MClassFunctionPair;
import com.septemberhx.common.base.MArchitectInfo;
import com.septemberhx.common.bean.MCompositionRequest;
import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.base.model.MSIInterface;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MServiceInterface;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.job.MCBuildJob;
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

        // composite the results
        MServerOperator operator = MSystemModel.getIns().getOperator();
        for (EndpointPair<MSIInterface> edge : resultList) {
            String instanceId1 = edge.nodeU().getInstanceId();
            String instanceId2 = edge.nodeV().getInstanceId();
            MServiceInstance instance1 = operator.getInstanceById(instanceId1);
            MServiceInstance instance2 = operator.getInstanceById(instanceId2);

            MService service1 = operator.getServiceById(instance1.getServiceId());
            MService service2 = operator.getServiceById(instance2.getServiceId());
            MServiceInterface interface1 = service1.getInterfaceById(edge.nodeU().getInterfaceName());
            MServiceInterface interface2 = service2.getInterfaceById(edge.nodeV().getInterfaceName());

            operator.compositeService(service1, interface1, service2, interface2);
        }
    }




}

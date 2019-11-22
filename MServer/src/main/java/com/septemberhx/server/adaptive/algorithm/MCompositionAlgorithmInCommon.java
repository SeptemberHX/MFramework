package com.septemberhx.server.adaptive.algorithm;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.septemberhx.common.base.MServiceInterface;
import com.septemberhx.common.base.MUser;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.server.adaptive.MAdaptiveSystem;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServerOperator;
import com.septemberhx.server.core.MServiceManager;
import com.septemberhx.server.core.MUserManager;
import com.septemberhx.server.utils.MModelUtils;
import org.javatuples.Triplet;

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

    public static Map<String, List<Triplet<MService, MServiceInterface, List<MUserDemand>>>> potentialPairListMap;

    public static void initPotentialPairList(List<MUserDemand> userDemandList, MServiceManager serviceManager, MUserManager userManager) {
        potentialPairListMap = new HashMap<>();
        for (MUserDemand userDemand : userDemandList) {
            MUser user = userManager.getById(userDemand.getUserId()).get();
            List<Triplet<MService, MServiceInterface, List<MUserDemand>>> potentialPairList = MModelUtils.getProperComServiceList(
                    userDemand, user.getContainedChain(userDemand.getId()), serviceManager
            );
            potentialPairListMap.put(userDemand.getId(), potentialPairList);
        }
    }

    public static void doCompositionPart(MutableValueGraph<MSInterface, Integer> interfaceGraph, MServerOperator prevOperator, MServerOperator currOperator) {
        List<EndpointPair<MSInterface>> edgeList = new ArrayList<>(interfaceGraph.edges());
        Collections.sort(edgeList, (o1, o2) ->
                -interfaceGraph.edgeValueOrDefault(o1, 0).compareTo(interfaceGraph.edgeValueOrDefault(o2, 0)));
        Integer allCallCount = 0;
        for (EndpointPair<MSInterface> edge : edgeList) {
            allCallCount += interfaceGraph.edgeValueOrDefault(edge, 0);
        }

        List<EndpointPair<MSInterface>> resultList = new ArrayList<>();
        for (EndpointPair<MSInterface> edge : edgeList) {
            if (interfaceGraph.edgeValueOrDefault(edge, 0) > allCallCount * MAdaptiveSystem.COMPOSITION_THRESHOLD) {
                resultList.add(edge);
            } else {
                break;
            }
        }

        // composite the results
        for (EndpointPair<MSInterface> edge : resultList) {
            String serviceId1 = edge.nodeU().getServiceId();
            String serviceId2 = edge.nodeV().getServiceId();

            MService service1 = prevOperator.getServiceById(serviceId1);
            MService service2 = prevOperator.getServiceById(serviceId2);

            // refuse the interface from the same service
            if (service1.getServiceName().equals(service2.getServiceName())) {
                continue;
            }

            MServiceInterface interface1 = service1.getInterfaceById(edge.nodeU().getInterfaceName());
            MServiceInterface interface2 = service2.getInterfaceById(edge.nodeV().getInterfaceName());

            currOperator.compositeService(service1, interface1, service2, interface2);
        }

        // todo: analyse prev system status, and remove useless service composition in previous system, mark them as not permitted
        // to keep the system as simple as possible, we will not try to add more interfaces in one composited service
        // and one time, only two interface will be composited. If it will be used in the future as part of chains, it will auto-enlarge.
    }




}

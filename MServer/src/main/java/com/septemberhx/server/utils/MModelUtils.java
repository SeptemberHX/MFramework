package com.septemberhx.server.utils;

import com.septemberhx.common.base.MDemandChain;
import com.septemberhx.common.base.MService;
import com.septemberhx.common.base.MServiceInterface;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.server.core.MServiceManager;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Yaml;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/9
 */
public class MModelUtils {

    public static List<Triplet<MService, MServiceInterface, List<MUserDemand>>> getProperComServiceList(MUserDemand userDemand, MDemandChain demandChain, MServiceManager serviceManager) {
        List<Triplet<MService, MServiceInterface, List<MUserDemand>>> serviceList = new ArrayList<>();
        if (demandChain == null) return serviceList;

        List<MService> comServiceList = serviceManager.getAllComServices();
        for (MService comService : comServiceList) {
            Pair<MServiceInterface, List<MUserDemand>> targetInterface = MModelUtils.checkCSvcSuitableForDChainWithSpecDemand(userDemand, demandChain, comService, serviceManager);
            if (targetInterface != null && targetInterface.getValue0() != null) {
                serviceList.add(new Triplet<>(comService, targetInterface.getValue0(), targetInterface.getValue1()));
            }
        }
        return serviceList;
    }

    private static Pair<MServiceInterface, List<MUserDemand>> checkCSvcSuitableForDChainWithSpecDemand(
            MUserDemand userDemand, MDemandChain chain, MService comService, MServiceManager serviceManager) {
        List<MUserDemand> demandList = chain.getDemandList();
        int targetIndex = chain.getDemandIndex(userDemand.getId());
        MServiceInterface targetInterface = null;
        List<MUserDemand> matchedDemandList = null;
        for (MServiceInterface comInterface : comService.getAllComInterfaces()) {
            List<String> comInterfaceIdList = comInterface.getCompositionList();
            if (demandList.size() < comInterfaceIdList.size()) {
                continue;
            }

            List<MServiceInterface> interfaceUnitList = comInterfaceIdList.stream().map(serviceManager::getInterfaceById).collect(Collectors.toList());
            for (int i = 0; i < interfaceUnitList.size(); ++i) {
                if (!userDemand.isServiceInterfaceMet(interfaceUnitList.get(i))) {
                    continue;
                }
                if (i > targetIndex) continue;
                if (demandList.size() - 1 - targetIndex < interfaceUnitList.size() - 1 - i) continue;

                boolean ifMatch = true;
                for (int j = 0, t = targetIndex - i; j < interfaceUnitList.size(); ++t, ++j) {
                    if (!demandList.get(t).isServiceInterfaceMet(interfaceUnitList.get(j))) {
                        ifMatch = false;
                        break;
                    }
                }
                if (ifMatch) {
                    targetInterface = comInterface;
                    matchedDemandList = demandList.subList(targetIndex - i, targetIndex - i + interfaceUnitList.size());
                    break;
                }
            }

            if (matchedDemandList != null && matchedDemandList.size() != 2) {
                System.out.println("Fuck!!!");
            }
        }
        return new Pair<>(targetInterface, matchedDemandList);
    }

    public static MService compService(MService service1, MServiceInterface interface1, MService service2, MServiceInterface interface2) {
        String serviceId = MIDUtils.generateServiceId(String.format("%s__%s", interface1.getInterfaceId(), interface2.getInterfaceId()),
                String.format("%s_%s", service1.getId(), service2.getId()));

        String serviceName = serviceId;
        String functionName = String.format("%s__%s", interface1.getFullFuncName(), interface2.getFullFuncName());
        MServiceInterface newInterface = new MServiceInterface();
        newInterface.setInterfaceId(MIDUtils.generateInterfaceId(serviceId, functionName));
        newInterface.setSlaLevel(-1);
        newInterface.setFunctionId(MIDUtils.generateFunctionId(functionName));
        newInterface.setServiceId(serviceId);
        newInterface.setInDataSize(interface1.getInDataSize());
        newInterface.setOutDataSize(interface2.getOutDataSize());

        List<String> compositionList = new ArrayList<>();
        if (interface1.isGenerated()) {
            compositionList.addAll(interface1.getCompositionList());
        } else {
            compositionList.add(interface1.getInterfaceId());
        }

        if (interface2.isGenerated()) {
            compositionList.addAll(interface2.getCompositionList());
        } else {
            compositionList.add(interface2.getInterfaceId());
        }
        newInterface.setCompositionList(compositionList);

        Map<String, MServiceInterface> interfaceMap = new HashMap<>();
        interfaceMap.put(newInterface.getInterfaceId(), newInterface);

        MService newService = new MService(serviceId, serviceName, null, interfaceMap);
        newService.setGenerated(true);
        newService.setMaxUserCap(Math.min(service1.getMaxUserCap(), service2.getMaxUserCap()));
        newService.setResource(service1.getResource().max(service2.getResource()));
        return newService;
    }

    public static V1Pod readPodYaml(String serviceName) {
        V1Pod pod = null;
        try {
            Object podYamlObj = Yaml.load(new File("/data/hexiang/yaml/" + serviceName + ".yaml"));
            if (podYamlObj.getClass().getSimpleName().equals("V1Pod")) {
                pod = (V1Pod) podYamlObj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pod;
    }

    public static void main(String[] args) {
        V1Pod v1Pod = MModelUtils.readPodYaml("shunfeng_service");
        System.out.println(v1Pod.toString());
    }
}

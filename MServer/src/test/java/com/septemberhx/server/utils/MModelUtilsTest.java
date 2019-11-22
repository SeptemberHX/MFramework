package com.septemberhx.server.utils;

import com.septemberhx.common.base.MDemandChain;
import com.septemberhx.common.base.MServiceInterface;
import com.septemberhx.common.base.MUser;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.server.base.model.*;
import com.septemberhx.server.core.MServiceManager;
import com.septemberhx.server.core.MUserManager;
import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/10
 */
class MModelUtilsTest {

    @Test
    public void checkCSvcSuitableForDChainWithSpecDemand() {
        MServiceManager serviceManager = MDataUtils.loadServiceManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\v2\\service.json");
        MUserManager userManager = MDataUtils.loadUserManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\v2\\user.json");

        MUser user = userManager.getById("user_10").get();
        MUserDemand demand = user.getDemandByDemandId("user_10_canteen_function_game");
        MDemandChain demandChain = user.getContainedChain(demand.getId());
        assertEquals(2, demandChain.getDemandList().size());

        MService service1 = serviceManager.getById("ali_service_r01").get();
        MServiceInterface serviceInterface1 = service1.getInterfaceById("ali_service_r01__canteen_function");

        MService service2 = serviceManager.getById("wechat_service_r01").get();
        MServiceInterface serviceInterface2 = service2.getInterfaceById("wechat_service_r01__pay_function");

        MService comService = MModelUtils.compService(service1, serviceInterface1, service2, serviceInterface2);
        serviceManager.add(comService);
        List<Triplet<MService, MServiceInterface, List<MUserDemand>>> r = MModelUtils.getProperComServiceList(demand, demandChain, serviceManager);
        assertNotEquals(0, r.size());
    }
}
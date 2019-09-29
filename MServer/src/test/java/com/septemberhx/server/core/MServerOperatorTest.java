package com.septemberhx.server.core;

import com.septemberhx.server.base.model.*;
import com.septemberhx.server.utils.MDataUtils;
import com.septemberhx.server.utils.MIDUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/28
 */
class MServerOperatorTest {

    @Test
    @Order(1)
    void addNewInstance() {
        String serviceId = "service1-111111";
        String nodeId = "node1";
        String instanceId = MIDUtils.generateSpecificInstanceIdForTest(nodeId, serviceId);

        assertTrue(MSystemModel.getIns().getServiceManager().containsById(serviceId));
        assertTrue(MSystemModel.getIns().getMSNManager().containsById(nodeId));
        assertFalse(MSystemModel.getIns().getMSIManager().containsById(instanceId));

        MServerOperator operator = MSystemModel.getIns().getOperator();
        MResource leftResource = operator.getNodeLeftResource(nodeId);
        Optional<MService> serviceOp = MSystemModel.getIns().getServiceManager().getById(serviceId);
        MService service = serviceOp.get();
        operator.addNewInstance(serviceId, nodeId, instanceId);
        assertEquals(operator.getNodeLeftResource(nodeId), leftResource.sub(service.getResource()));
    }

    @Test
    @Order(2)
    void assignDemandToIns() {
        MUserDemand userDemand = MSystemModel.getIns().getUserManager().getUserDemandByUserAndDemandId("user1", "user1-demand1");
        assertEquals(userDemand.getFunctionId(), "function1-111111");
        String instanceId = "service1-111111-123321";

        MServerOperator operator = MSystemModel.getIns().getOperator();
        Integer leftCap = operator.getInstanceLeftCap(instanceId);
        MDemandState newDemandState = operator.assignDemandToIns(userDemand, operator.getInstanceById(instanceId), null);
        assertEquals(newDemandState.getInterfaceId(), "service1_interface1");
        assertEquals(leftCap, operator.getInstanceLeftCap(instanceId) + 1);

        leftCap = operator.getInstanceLeftCap(instanceId);
        operator.assignDemandToIns(userDemand, operator.getInstanceById(instanceId), newDemandState);
        assertEquals(leftCap, operator.getInstanceLeftCap(instanceId));
    }

    @Test
    @Order(3)
    void deleteInstance() {
        String instanceId = "service1-111111-123321";
        MServerOperator operator = MSystemModel.getIns().getOperator();
        assertNotEquals(0, operator.getInstanceLeftCap(instanceId));
        assertNotNull(operator.getInstanceById(instanceId));
        String nodeId = operator.getInstanceById(instanceId).getNodeId();
        assertNotEquals(operator.getInstancesOnNode(nodeId).size(), 0);

        operator.deleteInstance(instanceId);
        assertEquals(0, operator.getInstanceLeftCap(instanceId));
        assertNull(operator.getInstanceById(instanceId));
        assertEquals(operator.getInstancesOnNode(nodeId).size(), 0);
    }

    @Test
    @Order(4)
    void getInstancesCanMetWithEnoughCapOnNode() {
        MServerOperator operator = MSystemModel.getIns().getOperator();
        MUserDemand userDemand = MSystemModel.getIns().getUserManager().getUserDemandByUserAndDemandId("user1", "user1-demand1");
        assertEquals(userDemand.getFunctionId(), "function1-111111");

        List<MServiceInstance> resultInstances = operator.getInstancesCanMetWithEnoughCapOnNode("node1", userDemand);
        assertEquals(resultInstances.size(), 1);
    }

    @Test
    @Order(5)
    void getAllSatisfiedService() {
        MServerOperator operator = MSystemModel.getIns().getOperator();
        MUserDemand userDemand = MSystemModel.getIns().getUserManager().getUserDemandByUserAndDemandId("user1", "user1-demand1");
        assertEquals(userDemand.getFunctionId(), "function1-111111");

        List<MService> resultServices = operator.getAllSatisfiedService(userDemand);
        assertEquals(resultServices.size(), 1);
    }

    @Test
    @Order(6)
    void ifInstanceHasCap() {
        String instanceId = "service1-111111-123321";
        MServerOperator operator = MSystemModel.getIns().getOperator();
        MServiceInstance instance = operator.getInstanceById(instanceId);
        assertNotNull(instance);

        MService service = operator.getServiceById(instance.getServiceId());
        assertNotNull(service);
        assertFalse(operator.ifInstanceHasCap(instanceId, service.getMaxUserCap() + 1));
        assertTrue(operator.ifInstanceHasCap(instanceId, service.getMaxUserCap()));
    }

    @BeforeEach
    void setUp() {
        MSystemModel.getIns().setServiceManager(MDataUtils.loadServiceManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\service.json"));
        MSystemModel.getIns().setUserManager(MDataUtils.loadUserManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\user.json"));
        MSystemModel.getIns().setMSNManager(MDataUtils.loadNodeManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\node.json",
                "D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\connection.json"));
        MSystemModel.getIns().setMSIManager(MDataUtils.loadInstanceManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\instance.json"));
        MSystemModel.getIns().getOperator().reInit();
    }
}
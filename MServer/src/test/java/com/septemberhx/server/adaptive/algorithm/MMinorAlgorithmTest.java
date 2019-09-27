package com.septemberhx.server.adaptive.algorithm;

import com.septemberhx.server.base.model.MUser;
import com.septemberhx.server.base.model.MUserDemand;
import com.septemberhx.server.core.MSystemModel;
import com.septemberhx.server.utils.MDataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/27
 */
class MMinorAlgorithmTest {
    @Test
    void testReplaceCompositionPart() {
        MMinorAlgorithm minorAlgorithm = new MMinorAlgorithm();
        MSystemModel.getIns().getOperator().reInit();
        Optional<MUser> user1 = MSystemModel.getIns().getUserManager().getById("user1");

        if (user1.isPresent()) {
            List<MUserDemand> param = user1.get().getDemandChainList().get(0).getDemandList();
            assertEquals(param.size(), 3);

            List<MUserDemand> result1 = minorAlgorithm.replaceCompositionPart(param);
            assertEquals(result1.size(), 2);
            assertEquals(result1.get(1).getFunctionId(), "function1_function2-111111");
        }
    }

    @BeforeEach
    void setUp() {
        MSystemModel.getIns().setServiceManager(MDataUtils.loadServiceManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\service.json"));
        MSystemModel.getIns().setUserManager(MDataUtils.loadUserManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\user.json"));
    }
}
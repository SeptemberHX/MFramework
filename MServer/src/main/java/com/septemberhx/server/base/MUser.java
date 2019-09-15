package com.septemberhx.server.base;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/11
 */
@Getter
@Setter
public class MUser extends MBaseObject {
    private List<MUserDemand> demandChain;      // user demand chain to satisfy user AT THIS TIME ONLY
}

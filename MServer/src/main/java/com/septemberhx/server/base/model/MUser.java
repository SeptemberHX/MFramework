package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/11
 */
@Getter
@Setter
public class MUser extends MBaseObject {
    private Map<String, MUserDemand> demandMap;      // user demand chain to satisfy user AT THIS TIME ONLY
}

package com.septemberhx.server.base;

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
public class MUser {
    private String userId;
    private List<MUserDemand> demandChain;      // user demand chain to satisfy user AT THIS TIME ONLY
}

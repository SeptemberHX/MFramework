package com.septemberhx.common.bean.server;

import com.septemberhx.common.base.MService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/24
 */
@Getter
@Setter
public class MRegisterServicesBean {
    private List<MService> serviceList;
    private boolean clearOldFlag;
}

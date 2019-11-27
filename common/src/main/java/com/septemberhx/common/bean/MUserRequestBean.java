package com.septemberhx.common.bean;

import com.septemberhx.common.base.MResponse;
import com.septemberhx.common.base.MUserDemand;
import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/22
 */
@Getter
@Setter
public class MUserRequestBean {
    private MUserDemand userDemand;
    private MResponse data;

    @Override
    public String toString() {
        return "MUserRequestBean{" +
                "userDemand=" + userDemand +
                ", data=" + data +
                '}';
    }
}

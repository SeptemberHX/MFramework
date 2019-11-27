package com.septemberhx.mgateway.bean;

import com.septemberhx.common.base.MUser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/11/27
 */
@Getter
@Setter
@ToString
public class MUserListBean {
    private List<MUser> userList;
}

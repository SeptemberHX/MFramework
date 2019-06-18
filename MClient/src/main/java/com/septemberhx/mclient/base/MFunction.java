package com.septemberhx.mclient.base;

import com.septemberhx.mclient.utils.StringUtils;

/**
 * @Author: septemberhx
 * @Date: 2018-12-12
 * @Version 0.1
 */
public class MFunction extends MObject {

    public MFunction() {
        if (this.getId() == null) {
            this.setId(StringUtils.generateMFunctionId());
        }
    }
}

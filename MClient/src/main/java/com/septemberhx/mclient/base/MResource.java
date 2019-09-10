package com.septemberhx.mclient.base;

import com.septemberhx.mclient.utils.StringUtils;

/**
 * @Author: septemberhx
 * @Date: 2018-12-09
 * @Version 0.1
 */
public class MResource extends MObject {

    public MResource() {
        if (this.getId() == null) {
            this.setId(StringUtils.generateMResourceId());
        }
    }
}

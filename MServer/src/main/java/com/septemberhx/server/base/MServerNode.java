package com.septemberhx.server.base;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MServerNode extends MBaseObject {
    private ServerNodeType nodeType;
}

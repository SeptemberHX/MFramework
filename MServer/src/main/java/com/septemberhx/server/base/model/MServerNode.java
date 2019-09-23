package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MServerNode extends MBaseObject {
    private ServerNodeType nodeType;
    private MResource resource;
    private MPosition position;
}

package com.septemberhx.server.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MServerNode extends MBaseObject {
    private ServerNodeType nodeType;
}

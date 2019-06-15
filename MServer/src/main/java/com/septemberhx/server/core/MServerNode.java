package com.septemberhx.server.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MServerNode {
    private String nodeId;
    private ServerNodeType nodeType;
}

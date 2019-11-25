package com.septemberhx.common.base;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MServerNode extends MBaseObject {
    private ServerNodeType nodeType;
    private MResource resource;
    private MPosition position;
    private Long delay;  // delay between direct user and node
    private Long bandwidth;  // bandwidth between direct user and node
    private String ip;

    @Override
    public String toString() {
        return "MServerNode{" +
                "nodeType=" + nodeType +
                ", resource=" + resource +
                ", position=" + position +
                ", delay=" + delay +
                ", bandwidth=" + bandwidth +
                ", ip='" + ip + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}

package com.septemberhx.server.base.model;

import com.septemberhx.common.base.MBaseObject;
import com.septemberhx.common.base.MPosition;
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

    @Override
    public String toString() {
        return "MServerNode{" +
                "nodeType=" + nodeType +
                ", resource=" + resource +
                ", position=" + position +
                ", delay=" + delay +
                ", bandwidth=" + bandwidth +
                ", id='" + id + '\'' +
                '}';
    }
}

package com.septemberhx.common.base;

import lombok.Getter;
import lombok.Setter;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/23
 */
@Getter
@Setter
public class MPosition {
    private Double x;
    private Double y;

    public Double distanceTo(MPosition o) {
        return Math.sqrt(Math.pow(this.x - o.x, 2) + Math.pow(this.y - o.y, 2));
    }

    @Override
    public String toString() {
        return "MPosition{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

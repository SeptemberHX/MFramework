package com.septemberhx.common.base;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MNodeConnectionInfo implements Comparable<MNodeConnectionInfo> {

    private long delay;
    private long bandwidth;

    @Override
    public int compareTo(MNodeConnectionInfo o) {
        if (delay != o.delay) {
            return Long.compare(this.delay, o.delay);
        } else {
            return Long.compare(o.bandwidth, this.bandwidth);
        }
    }

    public static int compare(MNodeConnectionInfo o1, MNodeConnectionInfo o2) {
        return o1.compareTo(o2);
    }

    @Override
    public String toString() {
        return "MNodeConnectionInfo{" +
                "delay=" + delay +
                ", bandwidth=" + bandwidth +
                '}';
    }
}

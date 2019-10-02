package com.septemberhx.common.base;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MArchitectInfo {
    private String groupId;
    private String artifactId;
    private String version;

    @Override
    public String toString() {
        return "MArchitectInfo{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

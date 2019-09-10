package com.septemberhx.common.base;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MDependency {
    private String groupId;
    private String artifactId;
    private String version;

    @Override
    public String toString() {
        return "MDependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

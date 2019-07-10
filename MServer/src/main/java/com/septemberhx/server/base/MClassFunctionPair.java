package com.septemberhx.server.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MClassFunctionPair {
    private String className;
    private String functionName;

    public MClassFunctionPair(String className, String functionName) {
        this.className = className;
        this.functionName = functionName;
    }

    public MClassFunctionPair() {
    }
}

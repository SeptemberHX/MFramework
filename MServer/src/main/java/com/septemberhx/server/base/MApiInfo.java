package com.septemberhx.server.base;

import com.septemberhx.common.base.MClassFunctionPair;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class MApiInfo {
    private String className;
    private String functionName;
    private String pattern;
    private List<MClassFunctionPair> compositionList;

    public MApiInfo(String className, String functionName, String pattern, List<MClassFunctionPair> compositionList) {
        this.className = className;
        this.functionName = functionName;
        this.pattern = pattern;
        this.compositionList = compositionList;
    }

    public MApiInfo(String className, String functionName, String pattern) {
        this(className, functionName, pattern, new ArrayList<>());
    }

    public void addCompositionPair(String className, String functionName) {
        this.compositionList.add(new MClassFunctionPair(className, functionName));
    }
}

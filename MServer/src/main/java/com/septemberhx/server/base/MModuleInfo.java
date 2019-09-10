package com.septemberhx.server.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class MModuleInfo {

    private String name;
    private List<MApiInfo> apiInfoList = new ArrayList<>();

    public MModuleInfo(String name) {
        this.name = name;
    }

    public void addApi(MApiInfo apiInfo) {
        this.apiInfoList.add(apiInfo);
    }

    public MApiInfo getApiInfoByClassNameAndFunctionName(String className, String functionName) {
        MApiInfo result = null;
        for (MApiInfo apiInfo : this.apiInfoList) {
            if (apiInfo.getClassName().equals(className) && apiInfo.getFunctionName().equals(functionName)) {
                result = apiInfo;
                break;
            }
        }
        return result;
    }
}

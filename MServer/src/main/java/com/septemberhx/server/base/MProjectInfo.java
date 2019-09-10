package com.septemberhx.server.base;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MProjectInfo {

    private String name;
    private String gitUrl;
    private Map<String, MModuleInfo> moduleInfoMap;

    public MProjectInfo(String name, String gitUrl, Map<String, MModuleInfo> moduleInfoMap) {
        this.name = name;
        this.gitUrl = gitUrl;
        this.moduleInfoMap = moduleInfoMap;
    }

    public MProjectInfo(String name, String gitUrl) {
        this(name, gitUrl, new HashMap<>());
    }

    public void addModule(MModuleInfo moduleInfo) {
        this.moduleInfoMap.put(moduleInfo.getName(), moduleInfo);
    }

    public MApiInfo getApiInfoByClassNameAndFunctionName(String className, String functionName) {
        MApiInfo result = null;
        for (MModuleInfo moduleInfo : moduleInfoMap.values()) {
            result = moduleInfo.getApiInfoByClassNameAndFunctionName(className, functionName);
            if (result != null) {
                break;
            }
        }
        return result;
    }
}

package com.septemberhx.common.base;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/12
 */
@Getter
@Setter
public class MServiceInterface {
    private String patternUrl;      // 1. controller url
    private String fullFuncName;    // 2. full function name with package prefix. Like com.test.func1
    private Integer slaLevel;       // 3. sla level
    private String functionId;      // 4. unique function id
    private Long cpuResource;       // 5. demand CPU resource       --- Not Used. Just Keep It
    private Long ramResource;       // 6. demand RAM resource       --- Not Used. Just Keep It
    private Integer maxUserNum;     // 7. max number it can serve at the same time  --- Not Used. Just Keep It
    private String interfaceId;     // 8. unique interface id
    private List<String> compositionList = new ArrayList<>();   // 9. Interface Id lists which are used to composite this interface
    private String serviceId;
    private Long inDataSize;
    private Long outDataSize;

    public boolean isGenerated() {
        return this.compositionList != null && !this.compositionList.isEmpty();
    }

    @Override
    public String toString() {
        return "MServiceInterface{" +
                "patternUrl='" + patternUrl + '\'' +
                ", fullFuncName='" + fullFuncName + '\'' +
                ", slaLevel=" + slaLevel +
                ", functionId='" + functionId + '\'' +
                ", cpuResource=" + cpuResource +
                ", ramResource=" + ramResource +
                ", maxUserNum=" + maxUserNum +
                ", interfaceId='" + interfaceId + '\'' +
                ", compositionList=" + compositionList +
                ", serviceId='" + serviceId + '\'' +
                ", inDataSize=" + inDataSize +
                ", outDataSize=" + outDataSize +
                '}';
    }

    private String getClassName() {
        int lastDotIndex = this.fullFuncName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new RuntimeException("Error, MServiceInterface.functionName is incomplete: " + this.fullFuncName);
        }

        return this.fullFuncName.substring(0, lastDotIndex);
    }

    private String getFunctionName() {
        int lastDotIndex = this.fullFuncName.lastIndexOf('.');
        return this.fullFuncName.substring(lastDotIndex + 1);
    }

    public MClassFunctionPair toClassFuncPair() {
        return new MClassFunctionPair(this.getClassName(), this.getFunctionName());
    }
}

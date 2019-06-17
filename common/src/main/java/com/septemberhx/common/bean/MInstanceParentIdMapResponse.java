package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class MInstanceParentIdMapResponse {
    private Map<String, String> parentIdMap;
}

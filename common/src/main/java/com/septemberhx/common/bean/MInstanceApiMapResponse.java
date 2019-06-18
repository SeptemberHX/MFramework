package com.septemberhx.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
public class MInstanceApiMapResponse {
    private Map<String, Set<String>> apiMap;
}

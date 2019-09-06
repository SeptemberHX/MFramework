package com.septemberhx.mclient.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class MResponse {
    private String status = "Success";
    private Map<String, Object> valueMap = new HashMap<>();

    public Object get(String key) {
        return this.valueMap.getOrDefault(key, null);
    }

    public MResponse set(String key, Object value) {
        this.valueMap.put(key, value);
        return this;
    }
}

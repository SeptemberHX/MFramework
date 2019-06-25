package com.septemberhx.common.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MObjectManager<T extends MBaseObject> {

    protected Map<String, T> objectMap;
    protected Logger logger = LogManager.getLogger(this.getClass());

    public MObjectManager() {
        this.objectMap = new HashMap<>();
    }

    public void add(T obj) {
        if (this.objectMap.containsKey(obj.getId())) {
            logger.warn("Object " + obj.getId() + " has already been added in Manager");
        }

        this.objectMap.put(obj.getId(), obj);
    }

    public Optional<T> getById(String id) {
        return Optional.of(this.objectMap.get(id));
    }

    public boolean containsById(String id) {
        return this.objectMap.containsKey(id);
    }
}

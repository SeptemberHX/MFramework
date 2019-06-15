package com.septemberhx.server.core;

import lombok.Getter;

public class MSnapshot {
    private static MSnapshot ourInstance = new MSnapshot();

    @Getter
    private MSystemModel systemModel;

    public static MSnapshot getInstance() {
        return ourInstance;
    }

    private MSnapshot() {
        this.systemModel = new MSystemModel();
    }
}

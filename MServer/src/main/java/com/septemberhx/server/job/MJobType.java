package com.septemberhx.server.job;

public enum MJobType {
    BUILD,
    CBUILD,
    DEPLOY,
    DELETE,
    MOVE,
    ADJUST,

    NOTIFY,
    SPLIT,
    SWITCH,
    BIGSWITCH,  // all the switch jobs
    COMPOSITE
}

package com.septemberhx.server.adaptive;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/26
 *
 * Enum to describe the state of the self-adaptive system.
 * Basically speaking, **the system can only have one state at any time**.
 */
public enum MSystemState {
    MONITING,
    ANALYZING,
    PLANNING,
    EXECUTING,
}

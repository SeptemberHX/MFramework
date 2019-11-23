package com.septemberhx.server.base;


import com.septemberhx.common.base.MUser;
import com.septemberhx.common.log.MBaseLog;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The input information which are needed by analyser
 */
@Getter
@Setter
public class MAnalyserInput {
    private List<MUser> userList;
    private List<MBaseLog> logList;

    public MAnalyserInput(List<MUser> userList, List<MBaseLog> logList) {
        this.userList = userList;
        this.logList = logList;
    }
}

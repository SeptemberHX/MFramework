package com.septemberhx.server.base;


import com.google.common.graph.MutableValueGraph;
import com.septemberhx.common.base.MUserDemand;
import com.septemberhx.server.adaptive.algorithm.MEvolveType;
import com.septemberhx.server.base.model.MSInterface;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The output of the analyser which will be fed into the planner
 */
@Getter
@Setter
public class MAnalyserResult {
    private MEvolveType evolveType;
    private Set<String> affectedUserIdByAvgTime;
    private Map<String, List<MUserDemand>> affectedUserId2MUserDemandsBySla;
    private MutableValueGraph<MSInterface, Integer> callGraph;
    private MutableValueGraph<MSInterface, Integer> allCallGraph;

    public MAnalyserResult() {
        this.evolveType = MEvolveType.NO_NEED;
    }

    public MAnalyserResult(MEvolveType evolveType) {
        this.evolveType = evolveType;
    }
}

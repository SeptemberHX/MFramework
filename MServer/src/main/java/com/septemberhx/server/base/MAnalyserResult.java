package com.septemberhx.server.base;


import com.google.common.graph.EndpointPair;
import com.septemberhx.server.adaptive.algorithm.MEvolveType;
import com.septemberhx.server.base.model.MDemandState;
import com.septemberhx.server.base.model.MSIInterface;
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
    private Map<String, List<MDemandState>> affectedUserId2MDemandStateBySla;
    private List<EndpointPair<MSIInterface>> potentialCompositionList;

    public MAnalyserResult() {
        this.evolveType = MEvolveType.NO_NEED;
    }

    public MAnalyserResult(MEvolveType evolveType) {
        this.evolveType = evolveType;
    }
}

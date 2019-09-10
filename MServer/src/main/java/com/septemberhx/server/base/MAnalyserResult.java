package com.septemberhx.server.base;


import com.septemberhx.server.adaptive.algorithm.MEvolveType;
import lombok.Getter;
import lombok.Setter;

/**
 * The output of the analyser which will be fed into the planner
 */
@Getter
@Setter
public class MAnalyserResult {
    private MEvolveType evolveType;

    public MAnalyserResult() {
        this.evolveType = MEvolveType.NO_NEED;
    }
}

package com.septemberhx.server.adaptive;

import com.septemberhx.common.log.MServiceBaseLog;
import com.septemberhx.server.base.MAnalyserResult;
import lombok.Getter;
import org.joda.time.DateTime;

import java.util.List;


/**
 * Analyse the input data to provider useful information for next step.
 * The result will be constructed as a MAnalyserOutput object
 */
public class MAnalyser {

    @Getter
    private long timeWindowInMillis;

    public MAnalyser() {
        this.timeWindowInMillis = 60 * 1000;
        System.out.println(DateTime.now().minus(this.timeWindowInMillis));
    }

    public MAnalyserResult analyse(List<MServiceBaseLog> logList) {
        return new MAnalyserResult();
    }

    public static void main(String[] args) {
        MAnalyser a = new MAnalyser();
    }
}

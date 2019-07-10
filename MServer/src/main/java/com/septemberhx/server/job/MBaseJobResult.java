package com.septemberhx.server.job;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MBaseJobResult {
    protected String jobId;
    protected MJobType type;
}

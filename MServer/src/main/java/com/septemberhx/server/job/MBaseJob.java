package com.septemberhx.server.job;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class MBaseJob extends MBaseObject {
    protected String parentId;
    protected MJobType type;
    protected Boolean completed;

    protected List<MBaseJob> subJobs = new ArrayList<>();

    public MBaseJob() {
        this.completed = false;
    }

    public void addSubJob(MBaseJob job) {
        job.setParentId(this.id);
        this.subJobs.add(job);
    }

    public MBaseJob nextJob() {
        int i = 0;
        for (; i < this.subJobs.size(); ++i) {
            if (!this.subJobs.get(i).getCompleted()) {
                break;
            }
        }

        if (i < this.subJobs.size()) {
            return this.subJobs.get(i);
        } else {
            return null;
        }
    }
}

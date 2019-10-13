package com.septemberhx.server.job;

import com.septemberhx.common.base.MBaseObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@ToString
public class MBaseJob extends MBaseObject {

    public enum MJobStatus {
        PENDING,
        DOING,
        DONE
    }

    protected String parentId;
    protected MJobType type;
    protected MJobStatus status;
    protected Integer priority;

    public static Integer BUILD = 1;
    public static Integer CBUILD = 1;
    public static Integer DEPLOY = 21;
    public static Integer DELETE = 21;
    public static Integer NOTIFY = 41;

    protected List<MBaseJob> subJobs = new ArrayList<>();

    public MBaseJob() {
        this.status = MJobStatus.PENDING;
        this.priority = 100;
    }

    public void addSubJob(MBaseJob job) {
        job.setParentId(this.id);
        this.subJobs.add(job);
    }

    public MBaseJob nextJob() {
        Collections.sort(this.subJobs, new Comparator<MBaseJob>() {
            @Override
            public int compare(MBaseJob o1, MBaseJob o2) {
                return Integer.compare(o1.priority, o2.priority);
            }
        });

        int doingPriority = -1;
        int i = 0;
        for (; i < this.subJobs.size(); ++i) {
            if (this.subJobs.get(i).isDoing()) {
                doingPriority = this.subJobs.get(i).getPriority();
            }

            if (this.subJobs.get(i).isPending()) {
                break;
            }
        }

        if (doingPriority < 0) {
            if (i < this.subJobs.size()) {
                return this.subJobs.get(i);
            } else {
                return null;
            }
        } else {
            if (i < this.subJobs.size() && this.subJobs.get(i).getPriority() == doingPriority) {
                return this.subJobs.get(i);
            } else {
                return null;
            }
        }
    }

    public double cost() {
        // todo: return cost of each operation
        return 0;
    }

    public void markAsDoing() {
        this.status = MJobStatus.DOING;
    }

    public void markAsDone() {
        this.status = MJobStatus.DONE;
    }

    public boolean isPending() {
        return this.status == MJobStatus.PENDING;
    }

    public boolean isDoing() {
        return this.status == MJobStatus.DOING;
    }

    public boolean isDone() {
        return this.status == MJobStatus.DONE;
    }
}

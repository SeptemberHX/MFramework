package com.septemberhx.server.job;

import com.septemberhx.common.bean.MBuildInfoRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class MBuildJob extends MBaseJob {
    private String projectName;
    private String moduleName;
    private String gitUrl;
    private String gitTag;
    private String branch;
    private String imageTag;

    private static String OWNER = "192.168.1.104:5000/septemberhx";

    public MBuildJob() {
        type = MJobType.BUILD;
        this.id = type.toString() + "_" + UUID.randomUUID().toString();
        this.priority = BUILD;
    }

    public String getImageName() {
        return projectName.toLowerCase() + "_" + moduleName.toLowerCase();
    }

    public String getImageFullName() {
        return OWNER + "/" + this.getImageName() + ":" + this.getImageTag();
    }

    public MBuildInfoRequest toBuildInfoRequest() {
        MBuildInfoRequest buildInfoRequest = new MBuildInfoRequest();
        buildInfoRequest.setGitUrl(gitUrl);
        buildInfoRequest.setBranch(branch);
        buildInfoRequest.setProjectName(projectName);
        buildInfoRequest.setModuleName(moduleName);
        buildInfoRequest.setImageName(this.getImageName());
        buildInfoRequest.setImageOwner(OWNER);
        buildInfoRequest.setImageTag(imageTag);
        buildInfoRequest.setId(id);
        return buildInfoRequest;
    }
}

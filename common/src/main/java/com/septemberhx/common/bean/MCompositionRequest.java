package com.septemberhx.common.bean;

import com.septemberhx.common.base.MClassFunctionPair;
import com.septemberhx.common.base.MDependency;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MCompositionRequest {

    private String id;
    private String name;
    private String docker_name;
    private String docker_owner;
    private String docker_tag;
    private String register_url;
    private List<MClassFunctionPair> chain_list;
    private List<MDependency> dependencies;

    @Override
    public String toString() {
        return "MCompositionRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", docker_name='" + docker_name + '\'' +
                ", register_url='" + register_url + '\'' +
                ", chain_list=" + chain_list +
                ", dependencies=" + dependencies +
                '}';
    }
}

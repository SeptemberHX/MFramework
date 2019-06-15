package com.septemberhx.test;

import com.septemberhx.mclient.base.MResource;

/**
 * @Author: septemberhx
 * @Date: 2018-12-11
 * @Version 0.1
 */
public class MResourceUser extends MResource {
    private String name;
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setNameAndAge(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public static void main(String[] args) {

    }
}

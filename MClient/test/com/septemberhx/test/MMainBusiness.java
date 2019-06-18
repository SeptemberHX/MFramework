package com.septemberhx.test;

import com.septemberhx.mclient.base.MBusiness;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
@RequestMapping("/MMainBusiness")
public class MMainBusiness extends MBusiness {

    private MResourceUser user;
    private MFunctionAdd addF;

    public MResourceUser getUser() {
        return user;
    }

    public void setUser(MResourceUser user) {
        this.user = user;
    }

    public MFunctionAdd getAddF() {
        return addF;
    }

    public void setAddF(MFunctionAdd addF) {
        this.addF = addF;
    }

    @RequestMapping("/*")
    public String func1() {
        return String.valueOf(addF.addInt(12, 13));
    }
}

package com.septemberhx.server.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.septemberhx.server.base.MApiInfo;
import com.septemberhx.common.base.MClassFunctionPair;
import com.septemberhx.server.base.MModuleInfo;
import com.septemberhx.server.base.MProjectInfo;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MRepoManager {
    private Map<String, MProjectInfo> projectInfoMap = new HashMap<>();

    public MApiInfo getApiInfoByClassNameAndFunctionName(String className, String functionName) {
        MApiInfo result = null;
        for (MProjectInfo projectInfo : this.projectInfoMap.values()) {
            result = projectInfo.getApiInfoByClassNameAndFunctionName(className, functionName);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public MApiInfo getApiInfo(MClassFunctionPair classFunctionPair) {
        return this.getApiInfoByClassNameAndFunctionName(classFunctionPair.getClassName(), classFunctionPair.getFunctionName());
    }

    public void addProject(MProjectInfo projectInfo) {
        this.projectInfoMap.put(projectInfo.getName(), projectInfo);
    }

    public void saveToFile(String filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fw = new FileWriter(filePath);
            fw.write(gson.toJson(this));
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MRepoManager loadFromFile(String filePath) {
        MRepoManager result = null;
        try {
            JsonReader fr = new JsonReader(new FileReader(filePath));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            result = gson.fromJson(fr, MRepoManager.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<MClassFunctionPair> breakApiChains(MApiInfo apiInfo, MClassFunctionPair classFunctionPair) {
        List<MClassFunctionPair> resultList = new ArrayList<>();
        boolean ifMeetBreakPoint = false;
        for (MClassFunctionPair pair : apiInfo.getCompositionList()) {
            if (pair.getClassName().equals(classFunctionPair.getClassName()) && pair.getFunctionName().equals(classFunctionPair.getFunctionName())) {
                ifMeetBreakPoint = true;
            }

            if (ifMeetBreakPoint) {
                resultList.add(pair);
            }
        }
        return resultList;
    }

    public static void g1() {
        MApiInfo mApiInfo1 = new MApiInfo("com.septemberhx.sampleservice2.controller.S2Controller", "wrapper", "/age");
        MModuleInfo mModuleInfo1 = new MModuleInfo("SampleService2");
        mModuleInfo1.addApi(mApiInfo1);

        MApiInfo mApiInfo2 = new MApiInfo("com.septemberhx.sampleservice1.controller.S1Controller","wrapper", "/wrapper");
        mApiInfo2.addCompositionPair("com.septemberhx.sampleservice1.controller.S1Controller", "wrapper");
        mApiInfo2.addCompositionPair("com.septemberhx.sampleservice2.controller.S2Controller", "wrapper");
        MModuleInfo mModuleInfo2 = new MModuleInfo("SampleService1");
        mModuleInfo2.addApi(mApiInfo2);

        MProjectInfo projectInfo1 = new MProjectInfo("SampleService", "http://192.168.1.104:12345/SeptemberHX/mframework.git");
        projectInfo1.addModule(mModuleInfo1);
        projectInfo1.addModule(mModuleInfo2);

        MRepoManager repoManager = new MRepoManager();
        repoManager.addProject(projectInfo1);
        repoManager.saveToFile("./project.json");
    }

//    public static void main(String[] args) {
//        MRepoManager repoManager = MRepoManager.loadFromFile("./project.json");
//        MApiInfo apiInfo = repoManager.getApiInfoByClassNameAndFunctionName("com.septemberhx.sampleservice1.controller.S1Controller", "wrapper");
//        System.out.println(repoManager.breakApiChains(apiInfo, "com.septemberhx.sampleservice2.controller.S2Controller", "wrapper"));
//    }
}

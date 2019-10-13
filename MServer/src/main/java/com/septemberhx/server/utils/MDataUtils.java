package com.septemberhx.server.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.septemberhx.server.base.MNodeConnectionInfo;
import com.septemberhx.server.base.model.MServerNode;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.server.base.model.MUser;
import com.septemberhx.server.core.MServerNodeManager;
import com.septemberhx.server.core.MServiceInstanceManager;
import com.septemberhx.server.core.MServiceManager;
import com.septemberhx.server.core.MUserManager;

import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/27
 */
public class MDataUtils {

    public static MServiceManager loadServiceManager(String jsonFilePath) {
        File f = new File(jsonFilePath);
        MServiceManager serviceManager = new MServiceManager();
        try {
            FileReader fileReader = new FileReader(f);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<MService> serviceList = gson.fromJson(fileReader, new TypeToken<List<MService>>() {}.getType());
            for (MService service : serviceList) {
                serviceManager.add(service);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serviceManager;
    }

    public static MUserManager loadUserManager(String jsonFilePath) {
        File f = new File(jsonFilePath);
        MUserManager userManager = new MUserManager();
        try {
            FileReader fileReader = new FileReader(f);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<MUser> userList = gson.fromJson(fileReader, new TypeToken<List<MUser>>() {}.getType());
            for (MUser user : userList) {
                userManager.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userManager;
    }

    public static MServerNodeManager loadNodeManager(String jsonFilePath, String connectionJsonFilePath) {
        File f = new File(jsonFilePath);
        MServerNodeManager nodeManager = new MServerNodeManager();
        try {
            FileReader fileReader = new FileReader(f);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<MServerNode> nodeList = gson.fromJson(fileReader, new TypeToken<List<MServerNode>>() {}.getType());
            for (MServerNode node : nodeList) {
                nodeManager.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File connectionF = new File(connectionJsonFilePath);
        try {
            FileReader fileReader = new FileReader(connectionF);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<MConnectionJson> nodeList = gson.fromJson(fileReader, new TypeToken<List<MConnectionJson>>() {}.getType());
            for (MConnectionJson info : nodeList) {
                nodeManager.addConnectionInfo(info.getConnection(), info.getSuccessor(), info.getPredecessor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodeManager;
    }

    public static MServiceInstanceManager loadInstanceManager(String jsonFilePath) {
        File f = new File(jsonFilePath);
        MServiceInstanceManager insManager = new MServiceInstanceManager();
        try {
            FileReader fileReader = new FileReader(f);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            List<MServiceInstance> insList = gson.fromJson(fileReader, new TypeToken<List<MServiceInstance>>() {}.getType());
            for (MServiceInstance inst : insList) {
                insManager.add(inst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return insManager;
    }

    public static void main(String[] args) {
        MServiceManager serviceManager = MDataUtils.loadServiceManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\service.json");
        for (MService service : serviceManager.getAllValues()) {
            System.out.println(service.toString());
        }

        MUserManager userManager = MDataUtils.loadUserManager("D:\\Workspace\\git\\MFramework\\MServer\\src\\test\\data\\user.json");
        for (MUser user : userManager.getAllValues()) {
            System.out.println(user.toString());
        }
    }

    class MConnectionJson {
        private String successor;
        private String predecessor;
        private MNodeConnectionInfo connection;

        public String getSuccessor() {
            return successor;
        }

        public void setSuccessor(String successor) {
            this.successor = successor;
        }

        public String getPredecessor() {
            return predecessor;
        }

        public void setPredecessor(String predecessor) {
            this.predecessor = predecessor;
        }

        public MNodeConnectionInfo getConnection() {
            return connection;
        }

        public void setConnection(MNodeConnectionInfo connection) {
            this.connection = connection;
        }
    }
}
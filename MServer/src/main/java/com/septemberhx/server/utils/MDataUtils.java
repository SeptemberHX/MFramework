package com.septemberhx.server.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.septemberhx.server.base.MNodeConnectionInfo;
import com.septemberhx.server.base.model.MServerNode;
import com.septemberhx.server.base.model.MService;
import com.septemberhx.server.base.model.MServiceInstance;
import com.septemberhx.common.base.MUser;
import com.septemberhx.server.core.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/9/27
 */
public class MDataUtils {

    private static String NODE_DATA_FILENAME = "node.json";
    private static String CONNECTION_DATA_FILENAME = "connection.json";
    private static String SERVICE_DATA_FILENAME = "service.json";
    private static String USER_PREV_DATA_FILENAME = "demand_prev.json";
    private static String USER_NEXT_DATA_FILENAME = "demand_curr.json";

    public static void loadDataFromDir(String dirPath, boolean prev) {
        MServerNodeManager nodeManager = MDataUtils.loadNodeManager(
                MDataUtils.joinPath(dirPath, NODE_DATA_FILENAME),
                MDataUtils.joinPath(dirPath, CONNECTION_DATA_FILENAME)
        );

        MServiceManager serviceManager = MDataUtils.loadServiceManager(
                MDataUtils.joinPath(dirPath, SERVICE_DATA_FILENAME)
        );
        serviceManager.verify();

        String userFileName = USER_PREV_DATA_FILENAME;
        if (!prev) {
            userFileName = USER_NEXT_DATA_FILENAME;
        }
        MUserManager userManager = MDataUtils.loadUserManager(
                MDataUtils.joinPath(dirPath, userFileName)
        );
        userManager.verify();

        MSystemModel.getIns().setMSNManager(nodeManager);
        MSystemModel.getIns().setServiceManager(serviceManager);
        MSystemModel.getIns().setUserManager(userManager);
    }

    private static String joinPath(String dirPath, String fileName) {
        return dirPath + "/" + fileName;
    }

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
                nodeManager.addConnectionInfo(info.getConnection(), info.getPredecessor(), info.getSuccessor());
            }

            MNodeConnectionInfo selfConnection = new MNodeConnectionInfo();
            selfConnection.setBandwidth(Long.MAX_VALUE);
            selfConnection.setDelay(0);
            for (MServerNode node : nodeManager.getAllValues()) {
                nodeManager.addConnectionInfo(selfConnection, node.getId(), node.getId());
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

    public static void saveServerOperatorToFile(MServerOperator serverOperator, String jsonFilePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonStr = gson.toJson(serverOperator);

        try {
            FileWriter fw = new FileWriter(jsonFilePath);
            fw.write(jsonStr);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MServerOperator loadServerOperator(String jsonFilePath) {
        File f = new File(jsonFilePath);
        MServerOperator serverOperator = new MServerOperator();
        try {
            FileReader fileReader = new FileReader(f);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            serverOperator = gson.fromJson(fileReader, MServerOperator.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverOperator;
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

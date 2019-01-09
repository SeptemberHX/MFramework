package core;

import io.undertow.Undertow;
import javafx.util.Pair;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author: septemberhx
 * @Date: 2018-12-18
 * @Version 0.1
 */
@SpringBootApplication
public class MMain {

    private static volatile MMain ourInstance = null;

    public static MMain getInstance() {
        if (ourInstance == null) {
            synchronized (MMain.class) {
                ourInstance = new MMain();
            }
        }
        return ourInstance;
    }

    private Map<Class<?>, List<Pair<String, String>>> mObjectFieldMap;

    public MMain() {
        this.mObjectFieldMap = new HashMap<Class<?>, List<Pair<String, String>>>();
        this.parseConfigXml("/Users/septemberhx/Workspace/git/MFramework/src/main/resources/config.xml");

        
    }

    private void parseConfigXml(String configXmlPath) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(configXmlPath);
            Element rootElement = document.getRootElement();

            List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                if (!"/MAdaptor/MObject".equals(element.getPath())) {
                    continue;
                }

                String className = element.element("class").getStringValue();
                Class<?> clazz = Class.forName(className);
                List<Pair<String, String>> filedPairList = new LinkedList<>();
                for (Element filed : element.elements("field")) {
                    String filedName = filed.element("name").getStringValue();
                    String filedClassStr = filed.element("class").getStringValue();
                    if (filedName == null || filedName.isEmpty()) break;

                    filedPairList.add(new Pair<>(filedName, filedClassStr));
                }
                this.mObjectFieldMap.put(clazz, filedPairList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Pair<String, String>> getMObjectFieldPairList(Class<?> clazz) {
        return this.mObjectFieldMap.getOrDefault(clazz, new LinkedList<>());
    }
}

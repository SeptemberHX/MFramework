package test;


import core.MMain;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: septemberhx
 * @Date: 2018-12-19
 * @Version 0.1
 */
@SpringBootApplication
public class ApplicationMain {

    public static void main(String[] args) {
        BasicConfigurator.configure();

//        MHttpMainController mmc = new MHttpMainController();

//        MMain mMain = MMain.getInstance();
        SpringApplication.run(ApplicationMain.class, args);
    }
}

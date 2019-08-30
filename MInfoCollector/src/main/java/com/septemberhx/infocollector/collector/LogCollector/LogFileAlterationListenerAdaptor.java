package com.septemberhx.infocollector.collector.LogCollector;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 */
public class LogFileAlterationListenerAdaptor extends FileAlterationListenerAdaptor {



    @Override
    public void onFileCreate(File file) {
        System.out.println(file.getName() + " created");
        Tailer.create(file, new LogFileTailerListener());
    }

    @Override
    public void onFileChange(File file) {
        System.out.println(file.getName() + " changed");
    }
}

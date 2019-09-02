package com.septemberhx.infocollector.collector.LogCollector;

import com.septemberhx.infocollector.collector.IInfoCollector;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.TimeUnit;

/**
 * @author SeptemberHX
 * @version 0.1
 * @date 2019/8/30
 *
 * This class is used to collect the log from the log files. It only collect the logs that the patterns can match.
 * Basically the log files are generated by the MClient module which will provide useful information for self-adaptive.
 */
public class LogFileCollector implements IInfoCollector {

    private static String LOG_DIR_PATH = "C:\\Users\\SeptemberHX\\Desktop\\work";
    private static long INTERVAL = TimeUnit.SECONDS.toMillis(1);
    private static String LOG_FILE_SUFFIX = ".log";

    private FileAlterationListenerAdaptor listenerAdaptor = new LogFileAlterationListenerAdaptor();

    @Override
    public void start() {
        // get all the files in the dir and set a Tailer for each log file
        File dirFile = new File(LOG_DIR_PATH);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        FilenameFilter fileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(LOG_FILE_SUFFIX);
            }
        };
        for (File file : dirFile.listFiles(fileFilter)) {
            Tailer.create(file, new LogFileTailerListener());
        }

        // match the dir
        IOFileFilter dir = FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE);

        // match the files which ends with .log
        IOFileFilter logFiles = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(LOG_FILE_SUFFIX));

        IOFileFilter filter = FileFilterUtils.or(dir, logFiles);
        FileAlterationObserver observer = new FileAlterationObserver(new File(LOG_DIR_PATH), filter);
        observer.addListener(listenerAdaptor);

        FileAlterationMonitor monitor = new FileAlterationMonitor(INTERVAL, observer);
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
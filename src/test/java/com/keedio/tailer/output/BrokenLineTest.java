package com.keedio.tailer.output;

import com.google.common.io.Files;
import com.keedio.tailer.conf.Configuration;
import com.keedio.tailer.validator.JsonValidator;
import com.keedio.tailer.validator.LineValidator;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * Created by luca on 11/2/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("brokenLineOutputProcessor")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class BrokenLineTest {
    @org.springframework.context.annotation.Configuration
    @Import(Configuration.class)
    static class TestConf {

        @Bean
        public LineValidator lineValidator(){
            return new JsonValidator();
        }

    }
    private final static Logger LOGGER = LogManager.getLogger(BrokenLineTest.class);

    private File logFile;

    @Value("${root.scan.dir}")
    private File logDir;

    static int lineNumber = -1;

    @Autowired
    private FileAlterationMonitor monitor;

    @Before
    public void init() throws Exception {
        lineNumber = -1;
        logFile = new File(logDir, "test.log");
        Files.createParentDirs(logFile);
    }

    @After
    public void destroy() throws Exception {
        monitor.stop(0);
        logFile.delete();
    }

    /**
     * Generates a constant string of data char by char, outputted to a temp file.
     */
    class DataGenerator implements Runnable {
        String data = "{\"log\":\"2016-02-02 10:53:22.285 [main] INFO  org.springframework.context.supp\rort.DefaultLifecycleProcessor - {\"category\":\"applicationlog\",\"requestId\":\"\",\"timestamp\": 2016-02-02 10:53:22.285,\"description\":\"Starting beans in phase 0\",\"returnCode\":\"\",\"trace\":\"\",\"appName\":\"bootstrap\",\"serverId\":\"null\"}\",\"stream\":\"stdout\",\"time\":\"2016-02-02T09:53:22.287849201Z\"}";
        long timeout;
        int maxLines;

        public DataGenerator(long timeout, int maxLines) {
            this.timeout = timeout;
            this.maxLines = maxLines;
        }

        @Override
        public void run() {

            System.out.println(data.length());
            LOGGER.info("Generating log to:\n" + logFile.getAbsolutePath());

            try (Writer w = new FileWriter(logFile)) {

                int k = 0;
                while (k < maxLines) {
                    for (int i = 0; i < data.length(); i++) {

                        if (i == 8) {
                            w.write(k + " ");
                        }

                        char c = data.charAt(i);
                        w.write(c);
                        w.flush();
                        Thread.sleep(timeout);
                    }
                    w.write('\n');
                    k++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                LOGGER.debug("Exiting Data generator");
            }
        }
    }

    @Test
    public void lineRollbackTestMultipleLines() throws Exception {
        Thread dataGenerator = new Thread(new DataGenerator(5,100));
        dataGenerator.start();

        Thread.sleep(1000);

        monitor.start();

        dataGenerator.join(10000);

        Assert.assertTrue("Read line number should be greater than 0, found: " + lineNumber, lineNumber > 0);
    }

}

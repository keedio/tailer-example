package com.keedio.tailer.output;

import com.google.common.io.Files;
import com.keedio.tailer.conf.Configuration;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Created by luca on 11/2/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("tailerRollbackLineOutputProcessor")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TailerRollbackLineTest {
    @org.springframework.context.annotation.Configuration
    @Import(Configuration.class)
    static class TestConf implements LineValidator{
        private static final String regexp = "^\\[TRACE\\].*\\)$";
        private static final Pattern pattern = Pattern.compile(regexp);

        @Override
        public boolean isValid(String partialLine) {
            boolean res = pattern.matcher(partialLine).matches();

            if (res){
                //LOGGER.info("Found match on line: " +partialLine);
            }

            return res;
        }
    }

    private final static Logger LOGGER = LogManager.getLogger(TailerRollbackLineTest.class);

    private File logFile;

    @Value("${root.scan.dir}")
    private File logDir;

    private static int lineNumber = -1;

    public static void setLineNumber(int num){
        lineNumber = num;
    }

    public static int getLineNumber(){
        return lineNumber;
    }

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
        String data = "[TRACE] 2016-02-09 16:41:09.873 [pool-3-thread-1] out - \\nAccountTransaction(1455032469864,1455032469864,SzptpSNPWNVqojsHPbYH,0399158778252679 05623732,bIowQQUwFLBRbbb,329475618292398,2,-1180.0,40861.41,None,0.0)";

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
                    w.write("\n");
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

        Thread.sleep(3000);

        monitor.start();

        dataGenerator.join(10000);

        Assert.assertTrue("Read line number should be greater than 0, found: " + lineNumber, lineNumber > 0);
    }

    @Test
    public void lineRollbackTestOneLine() throws Exception {

        Thread dataGenerator = new Thread(new DataGenerator(10,1));
        dataGenerator.start();

        monitor.start();

        Thread.sleep(3000);

        dataGenerator.join(2000);
        assertEquals("Read line number should be 0", 0, lineNumber);
    }
}

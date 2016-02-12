package com.keedio.tailer.output;

import com.google.common.io.Files;
import com.keedio.tailer.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * Created by luca on 11/2/16.
 */
public class TailerRollbackLineTest {
    private final static Logger LOGGER = LogManager.getLogger(TailerRollbackLineTest.class);

    private File logFile;
    private File logDir;

    static int lineNumber = -1;

    @Before
    public void init(){
        lineNumber = -1;
        logDir = Files.createTempDir();
        logFile = new File(logDir, "test.log");
    }

    /**
     * Generates a constant string of data char by char, outputted to a temp file.
     */
    class DataGenerator implements Runnable {
        String data = "[TRACE] 2016-02-09 16:41:09.873 [pool-3-thread-1] out - \\nAccountTransaction(1455032469864,1455032469864,SzptpSNPWNVqojsHPbYH,0399158778252679 05623732,bIowQQUwFLBRbbb,329475618292398,2,-1180.0,40861.41,None,0.0)";

        long timeout;

        public DataGenerator(long timeout){
            this.timeout = timeout;
        }

        @Override
        public void run() {

            LOGGER.info("Generating log to:\n" + logFile.getAbsolutePath());

            try (Writer w = new FileWriter(logFile)){

                int k = 0;
                while (true) {
                    for (int i = 0; i < data.length(); i++) {

                        if (i == 8){
                            w.write(k+ " ");
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

        System.setProperty("spring.profiles.active", "tailerRollbackLineOutputProcessor");
        System.setProperty("root.scan.dir", logDir.getAbsolutePath());

        Thread dataGenerator = new Thread(new DataGenerator(5));
        dataGenerator.start();

        Thread.sleep(3000);

        Main.main(null);

        dataGenerator.join(3000);

        Assert.assertTrue("Read line number should be greater than 0, found: " + lineNumber, lineNumber > 0);
    }

    /*
    @Test
    public void lineRollbackTestOneLine() throws Exception {

        System.setProperty("spring.profiles.active", "tailerRollbackLineOutputProcessor");
        System.setProperty("root.scan.dir", logDir.getAbsolutePath());

        Thread dataGenerator = new Thread(new DataGenerator(10));
        dataGenerator.start();

        Thread.sleep(1000);

        dataGenerator.join(3000);
        Assert.assertEquals("Read line number should be 0", 0, lineNumber);
    }
    */
}

package com.keedio.tailer.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by luca on 12/2/16.
 */
@Component
@Profile("tailerRollbackLineOutputProcessor")
class TailerRollbackLineOutputProcessor implements TailerOutputProcessor {
    private final static Logger LOGGER = LogManager.getLogger(TailerRollbackLineOutputProcessor.class);

    @Override
    public void process(String line) {

        if (!line.endsWith("40861.41,None,0.0)") || !line.startsWith("[TRACE]")) {

            LOGGER.error("Read line is not valid: " + line);
            throw new RuntimeException("Aborting");

        } else if(TailerRollbackLineTest.lineNumber == -1) {
            TailerRollbackLineTest.lineNumber = Integer.parseInt("" + line.charAt(8));
        }
    }
}

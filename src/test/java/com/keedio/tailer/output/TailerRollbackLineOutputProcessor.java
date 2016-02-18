package com.keedio.tailer.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.keedio.tailer.output.TailerRollbackLineTest.setLineNumber;

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

            throw new RuntimeException("Read line is not valid: " + line);

        } else {
            LOGGER.info("Valid line detected: " + line);

        }
    }
}

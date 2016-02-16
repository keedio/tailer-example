package com.keedio.tailer.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by luca on 12/2/16.
 */
@Component
@Profile("brokenLineOutputProcessor")
class BrokenLineOutputProcessor implements TailerOutputProcessor {
    private final static Logger LOGGER = LogManager.getLogger(BrokenLineOutputProcessor.class);

    @Override
    public void process(String line) {

        if (!line.startsWith("{\"log\":")) {
            throw new RuntimeException("Read line is not valid: " + line);

        }
    }
}

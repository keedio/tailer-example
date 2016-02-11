package com.keedio.tailer.output;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by luca on 11/2/16.
 */
@Component
@Profile("loggerOutput")
public class LoggerOutputProcessor implements TailerOutputProcessor {

    @Value("${output.logger.name:}")
    private String loggerName;

    @Value("${output.logger.log.level:TRACE}")
    private Level logLevel;

    private Logger logger;

    @PostConstruct
    private void init() {
        logger = LogManager.getLogger(loggerName);
    }

    @Override
    public void process(String line) {
        logger.log(logLevel, line);
    }
}

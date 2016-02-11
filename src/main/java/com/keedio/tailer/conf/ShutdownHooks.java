package com.keedio.tailer.conf;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by luca on 10/2/16.
 */
@Component
public class ShutdownHooks {
    private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(ShutdownHooks.class);

    @Autowired
    private FileAlterationMonitor monitor;

    @PostConstruct
    public void init(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    LOGGER.info("Stopping monitor");
                    monitor.stop(0);
                } catch (Exception e) {
                    LOGGER.error("Exception while stopping monitor",e);
                }
            }
        });
    }
}

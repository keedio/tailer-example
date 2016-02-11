package com.keedio.tailer.output;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by luca on 11/2/16.
 */
@Component
@Profile("testProcessor")
public class TestOutputProcessor implements TailerOutputProcessor{
    @Override
    public void process(String line) {

    }
}

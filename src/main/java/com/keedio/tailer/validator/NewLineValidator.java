package com.keedio.tailer.validator;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by luca on 15/2/16.
 */
@Component
@Profile("newlineValidator")
public class NewLineValidator implements LineValidator{
    @Override
    public boolean isValid(String line) {
        return true;
    }
}

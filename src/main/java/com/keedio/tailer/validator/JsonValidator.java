package com.keedio.tailer.validator;

import com.google.gson.Gson;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by luca on 15/2/16.
 */
@Component
@Profile("jsonValidator")
public class JsonValidator implements LineValidator {

    private Gson gson = new Gson();

    @Override
    public boolean isValid(String line) {
        try {
            gson.fromJson(line, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }
}

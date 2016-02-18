package com.keedio.tailer.validator;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.regex.Pattern;

/**
 * Created by luca on 15/2/16.
 */
@Component
@Profile("jsonValidator")
public class JsonValidator implements LineValidator {
    private final static Logger LOGGER = LogManager.getLogger(JsonValidator.class);

    private Gson gson = new Gson();

    private final String regexp = "^\\{.*\\}$";
    private final Pattern pattern = Pattern.compile(regexp);

    @PostConstruct
    public void init(){
        LOGGER.info("Initializing " + this.getClass().getCanonicalName());
    }

    @Override
    public boolean isValid(String line) {
        try {
            gson.fromJson(line, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {

            if (pattern.matcher(line).matches()){
                LOGGER.error("Input line matches "+regexp+" but invalid for com.google.gson.Gson",ex);
            }
            return false;
        }
    }
}

package com.example.odontologia_api.config;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimezoneConfig {

    private static final String APP_TIMEZONE = "America/La_Paz";

    @PostConstruct
    void setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone(APP_TIMEZONE));
    }
}

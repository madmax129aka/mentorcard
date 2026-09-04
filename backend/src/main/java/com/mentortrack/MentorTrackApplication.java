package com.mentortrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MentorTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentorTrackApplication.class, args);
    }
}

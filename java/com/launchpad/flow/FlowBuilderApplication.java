package com.launchpad.flow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FlowBuilderApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowBuilderApplication.class, args);
    }
}


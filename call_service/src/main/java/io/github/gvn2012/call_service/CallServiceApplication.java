package io.github.gvn2012.call_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CallServiceApplication.class, args);
    }
}

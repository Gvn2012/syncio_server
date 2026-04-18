package io.github.gvn2012.post_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class PostServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostServiceApplication.class, args);
    }

}

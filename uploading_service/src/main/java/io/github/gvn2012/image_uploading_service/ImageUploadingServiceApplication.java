package io.github.gvn2012.image_uploading_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ImageUploadingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageUploadingServiceApplication.class, args);
    }

}

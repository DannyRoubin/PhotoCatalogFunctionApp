package org.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.app.services.ImageService;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {

        String endpoint = Secrets.getEndpoint();
        String key = Secrets.getKey();

        System.out.println("Running Computer Vision analysis on sample.jpg...");
        String result = ImageService.analyzeSampleImage(endpoint, key);
        System.out.println("Tags from sample.jpg: " + result);

        SpringApplication.run(Main.class, args);

    }
}

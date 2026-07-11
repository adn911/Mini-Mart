package com.minimart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiniMartApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiniMartApplication.class, args);
    }
}

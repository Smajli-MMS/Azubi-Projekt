package com.mediamarktsaturn.azubi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ensar.relin.playground")
public class RelinApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelinApplication.class, args);
    }
}
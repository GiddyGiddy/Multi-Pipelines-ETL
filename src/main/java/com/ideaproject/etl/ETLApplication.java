package com.ideaproject.etl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ETLApplication {

    public static void main(String[] args) {
        SpringApplication.run(ETLApplication.class, args);
    }
}


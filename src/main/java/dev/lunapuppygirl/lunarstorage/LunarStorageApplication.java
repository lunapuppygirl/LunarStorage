package dev.lunapuppygirl.lunarstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
public class LunarStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(LunarStorageApplication.class, args);
    }

}

package dev.lunapuppygirl.lunarstorage.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {
    @Bean("executor")
    public Executor executor() {
        return Executors.newFixedThreadPool(2);
    }
}

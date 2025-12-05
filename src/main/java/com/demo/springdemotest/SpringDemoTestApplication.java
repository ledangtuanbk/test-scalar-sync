package com.demo.springdemotest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableCaching
@EntityScan("com.demo.mylib.domain")
@EnableJpaRepositories("com.demo.mylib.repo")
@SpringBootApplication(scanBasePackages = {"com.demo.springdemotest", "com.demo.mylib"})
public class SpringDemoTestApplication implements CommandLineRunner {

    @Value("${demo.value}")
    private String demoValue;

    public static void main(String[] args) {
        SpringApplication.run(SpringDemoTestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Demo Value: " + demoValue);
    }
}

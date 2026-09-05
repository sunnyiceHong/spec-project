package com.example.withdrawal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the withdrawal service demo application.
 *
 * <p>Scans the {@code com.example} base package so the {@code com.example.withdraw}
 * feature beans (REST controller, service, in-memory repository) are wired
 * alongside this class.</p>
 */
@SpringBootApplication(scanBasePackages = "com.example")
public class WithdrawalApplication {

    public static void main(String[] args) {
        SpringApplication.run(WithdrawalApplication.class, args);
    }
}

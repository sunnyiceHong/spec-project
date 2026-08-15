package com.example.withdrawal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the withdrawal service demo application.
 *
 * <p>The demo focuses on the service layer (the Spec-Contract-First TDD/BDD
 * workflow), so no {@code @RestController} is required. The in-memory
 * repositories and the withdrawal service are exposed as Spring beans so a
 * controller can be added later without changing any existing code.</p>
 */
@SpringBootApplication
public class WithdrawalApplication {

    public static void main(String[] args) {
        SpringApplication.run(WithdrawalApplication.class, args);
    }
}

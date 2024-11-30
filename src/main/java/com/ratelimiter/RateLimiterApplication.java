package com.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
public class RateLimiterApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RateLimiterApplication.class, args);

        // Get the RateLimiter bean
        RateLimiter rateLimiter = context.getBean(RateLimiter.class);

        // Test the rate limiter
        String userId = "testUser";
        int maxRequests = 5;
        int windowSeconds = 60;

        System.out.println("Testing Rate Limiter for user: " + userId);
        System.out.println("Max requests: " + maxRequests + " per " + windowSeconds + " seconds");

        // Try 7 requests (should see 5 allowed and 2 blocked)
        for (int i = 1; i <=7; i++) {
            boolean allowed = rateLimiter.isAllowed(userId, maxRequests, windowSeconds);
            System.out.println("Request " + i + ": " + (allowed ? "Allowed" : "Blocked"));

            try {
                Thread.sleep(500); // Small delay between requests
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\nWaiting for window to reset...");
        try {
            Thread.sleep(windowSeconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Try one more request after window reset
        boolean allowed = rateLimiter.isAllowed(userId, maxRequests, windowSeconds);
        System.out.println("After window reset: " + (allowed ? "Allowed" : "Blocked"));
    }
}

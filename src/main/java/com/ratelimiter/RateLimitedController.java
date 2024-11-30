package com.ratelimiter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class RateLimitedController {
    private final RateLimiter rateLimiter;

    public RateLimitedController(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint(HttpServletRequest request) {
        String clientId = request.getRemoteAddr();

        if (rateLimiter.isAllowed(clientId, 10, 60)) { // 10 requests per 60 seconds
            return ResponseEntity.ok("Request processed successfully");
        } else {
            return ResponseEntity.status(429).body("Too many requests");
        }
    }
}

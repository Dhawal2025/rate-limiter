package com.ratelimiter;


import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;

    public RateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, int maxRequests, int windowInSeconds) {
        String redisKey = "rate_limit:" + key;
        long currentTimestamp = Instant.now().getEpochSecond();

        // Using SessionCallback to execute transaction
        SessionCallback<Boolean> callback = new SessionCallback<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            public Boolean execute(RedisOperations operations) {
                operations.multi();

                try {
                    // Remove old timestamps
                    operations.opsForZSet().removeRangeByScore(redisKey, 0, currentTimestamp - windowInSeconds);

                    // Count current requests
                    operations.opsForZSet().count(redisKey, 0, currentTimestamp);

                    // Add current timestamp (will be executed only if transaction succeeds)
                    operations.opsForZSet().add(redisKey, String.valueOf(currentTimestamp), currentTimestamp);

                    // Set expiry
                    operations.expire(redisKey, Duration.ofSeconds(windowInSeconds));

                    // Execute transaction and get results
                    List<Object> results = operations.exec();

                    if (results == null) {
                        return false; // Transaction failed
                    }

                    // Get the count from results (it's the second result in our transaction)
                    Long count = (Long) results.get(1);
                    return count < maxRequests;

                } catch (Exception e) {
                    operations.discard();
                    throw new RuntimeException("Rate limiting failed", e);
                }
            }
        };

        return Boolean.TRUE.equals(redisTemplate.execute(callback));
    }
}
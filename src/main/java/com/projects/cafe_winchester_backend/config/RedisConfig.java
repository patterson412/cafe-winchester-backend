package com.projects.cafe_winchester_backend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")   // When the container is going to be destroyed the shudown method of the RedissonClient is called ensuring that resources such as connections are released
    public RedissonClient redisson() {
        Config config = new Config();
        // Format: redis://username:password@host:port
        String address = String.format("redis://%s:%d", redisHost, redisPort);
        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisPassword)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(5)    // This setting defines the minimum number of idle (unused) connections that should be kept in the pool at all times.
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(7000); // Sets the timeout to 7000 milliseconds (7 seconds). Defines how long the client will wait for a response from the Redis server before considering the command as failed

        return Redisson.create(config);
    }
}
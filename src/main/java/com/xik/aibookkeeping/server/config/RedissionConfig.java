package com.xik.aibookkeeping.server.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissionConfig {
    @Value("${ai-bookkeeping.redis.host}")
    private String redisHost;

    @Value("${ai-bookkeeping.redis.port}")
    private String redisPort;

    @Value("${ai-bookkeeping.redis.password}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setPassword(redisPassword)
                .setDatabase(0);
        return Redisson.create(config);
    }
}

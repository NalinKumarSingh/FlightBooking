package com.example.flight.v1.configure;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()
        .setAddress("redis://default:gQAAAAAAAY0NAAIocDI3Yjc4M2JlM2VjNTU0MWE1YjNlZDA5MWNhYjhjNzM1NnAyMTAxNjQ1@magnetic-blowfish-101645.upstash.io:6379");
    return Redisson.create(config);
  }
}

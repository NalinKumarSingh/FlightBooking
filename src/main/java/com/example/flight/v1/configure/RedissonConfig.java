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
        .setAddress("rediss://default:Ad1GAAIncDExZDY0MDkzZjViZGU0YTVjODhjYWIwYTZmYzg0ZGZkZXAxNTY2NDY@emerging-mallard-56646.upstash.io:6379");
    return Redisson.create(config);
  }
}

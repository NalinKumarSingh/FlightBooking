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
        .setAddress("redis://default:MZnYhikQoDRHeyKahBPyexqywxvxsfZt@interchange.proxy.rlwy.net:44885");
    return Redisson.create(config);
  }
}
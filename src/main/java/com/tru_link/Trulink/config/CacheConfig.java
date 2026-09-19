package com.tru_link.Trulink.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("urlCache");


        cacheManager.registerCustomCache(
                "urlCache",
                Caffeine.newBuilder()
                        .maximumSize(50_000)
                        .expireAfterWrite(Duration.ofHours(24))
                        .recordStats()
                        .build());

        return cacheManager;
    }
}

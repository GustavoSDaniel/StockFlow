package com.gustavosdaniel.stock_flow_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.web.reactive.config.EnableWebFlux;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring configuration for Redis-based caching.
 * <p>
 * Provides a reactive Redis template with JSON serialization for cache operations,
 * a configurable default TTL for cache entries, and a system-default clock for
 * time-sensitive cache eviction.
 * </p>
 */
@Configuration
public class CacheConfig {

    @Value("${cache.ttl.default}")
    private long defaultTtl;

    /**
     * Creates a reactive Redis template configured with JSON serialization.
     * <p>
     * Keys are serialized as plain strings; values and hash entries use
     * {@link GenericJacksonJsonRedisSerializer} to preserve type information
     * across cache read/write cycles.
     * </p>
     *
     * @param factory      the reactive Redis connection factory
     * @param objectMapper the Jackson ObjectMapper used for value serialization
     * @return a fully configured {@link ReactiveRedisTemplate}
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper objectMapper){

        GenericJacksonJsonRedisSerializer jsonSerializer =
                new GenericJacksonJsonRedisSerializer(objectMapper);

        RedisSerializationContext<String,Object> context =
                RedisSerializationContext.<String, Object>newSerializationContext()
                        .key(RedisSerializer.string())
                        .value(jsonSerializer)
                        .hashKey(RedisSerializer.string())
                        .hashValue(jsonSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);

    }

    /**
     * Exposes the cache TTL from application properties as a {@link Duration} bean.
     *
     * @return the default cache entry time-to-live
     */
    @Bean
    public Duration defaultCacheTtl(){

        return Duration.ofSeconds(defaultTtl);
    }

    /**
     * Provides a system-default clock bean for time-dependent cache eviction logic.
     *
     * @return a {@link Clock} set to the JVM's default time zone
     */
    @Bean
    public Clock clock(){

        return Clock.systemDefaultZone();
    }
}



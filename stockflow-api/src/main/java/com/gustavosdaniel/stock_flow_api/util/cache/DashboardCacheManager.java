package com.gustavosdaniel.stock_flow_api.util.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class DashboardCacheManager {

    private static final Logger log = LoggerFactory.getLogger(DashboardCacheManager.class);

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private record CacheEntry<T>(T value, Instant expiresAt) {

        boolean isExpired(Clock clock) {
            return Instant.now(clock).isAfter(expiresAt);
        }
    }

    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<Mono<?>>> refreshLocks = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration ttl;

    public DashboardCacheManager(Clock clock) {
        this.clock = clock;
        this.ttl = DEFAULT_TTL;
    }

    @SuppressWarnings("unchecked")
    public <T> Mono<T> getOrCompute(String key, Mono<T> supplier){

        CacheEntry<?> entry = cache.get(key);

        if (entry != null && !entry.isExpired(clock)){

            log.debug("Cache HIT key = {}", key);

            return Mono.just((T) entry.value);
        }

        log.debug("Cache MISS key = {} (refresh em andamento ou expirado)", key);

        AtomicReference<Mono<?>> lock = refreshLocks
                .computeIfAbsent( key, k -> new AtomicReference<>()

                );

        Mono<?> inFlight = lock.get();

        if (inFlight != null) {

            log.debug("Cache WAIT — key={} (reutilizando refresh em andamento)", key);

            return (Mono<T>) inFlight;
        }

        Mono<T> fresh = supplier
                .doOnNext(value -> {
                    cache.put(key, new CacheEntry<>(value, Instant.now(clock).plus(ttl)));
                    log.debug("Cache STORE key = {}, expiresAt = {}", key, Instant.now(clock).plus(ttl));
                })
                .doOnError(error -> log.warn("Cache ERROR — key={}: {}", key, error.getMessage()))
                .doFinally(__ -> {
                    lock.set(null);
                    refreshLocks.remove(key);
                })
                .cache();

        lock.set(fresh);
        return fresh;
    }

    public void evict(String key){

        cache.remove(key);
        log.debug("Cache EVICT  key = {}", key);
    }

    public void evictAllDashboards(){

        cache.keySet().removeIf(k -> k.startsWith(DashboardCacheKeys.PREFIX));
        log.info("Cache EVICT ALL  prefix = {}", DashboardCacheKeys.PREFIX);
    }

}

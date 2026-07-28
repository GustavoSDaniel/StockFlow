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

/**
 * Thread-safe, in-memory cache manager with TTL expiration and deduplication
 * of concurrent refresh requests.
 * <p>
 * Each cached entry has a default time-to-live (TTL) of 5 minutes. Concurrent
 * requests for the same expired/missing key share a single refresh operation,
 * avoiding redundant computation (thundering herd protection).
 * </p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 * dashboardCacheManager.getOrCompute(DashboardCacheKeys.OVERVIEW, fetchOverview());
 * dashboardCacheManager.evict(DashboardCacheKeys.OVERVIEW);
 * dashboardCacheManager.evictAllDashboards();
 * }</pre>
 */
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

    /**
     * Creates a cache manager with the default TTL of 5 minutes.
     *
     * @param clock the clock used for TTL expiration checks
     */
    public DashboardCacheManager(Clock clock) {
        this.clock = clock;
        this.ttl = DEFAULT_TTL;
    }

    /**
     * Retrieves a cached value or computes and caches it if absent or expired.
     * <p>
     * If another thread is already refreshing the same key, this call waits
     * for and shares the result of that in-flight refresh instead of starting
     * a duplicate computation.
     * </p>
     *
     * @param <T>      the cached value type
     * @param key      the cache key
     * @param supplier a {@link Mono} that computes the value when cache is cold
     * @return a {@link Mono} emitting the cached or freshly computed value
     */
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

    /**
     * Evicts a single entry from the cache by its key.
     *
     * @param key the cache key to evict
     */
    public void evict(String key){

        cache.remove(key);
        log.debug("Cache EVICT  key = {}", key);
    }

    /**
     * Evicts all dashboard-related entries from the cache (keys starting
     * with the {@link DashboardCacheKeys#PREFIX}).
     */
    public void evictAllDashboards(){

        cache.keySet().removeIf(k -> k.startsWith(DashboardCacheKeys.PREFIX));
        log.info("Cache EVICT ALL  prefix = {}", DashboardCacheKeys.PREFIX);
    }

}

package com.gustavosdaniel.stock_flow_api.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Utility class that builds Spring Data {@link Page} objects from reactive
 * types ({@link Flux} and {@link Mono}).
 * <p>
 * Designed as a static helper; the constructor is private to prevent
 * instantiation.
 * </p>
 */
public class PageUtils {

    private PageUtils(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Combines a {@link Flux} of data and a {@link Mono} of total count into
     * a single {@link Page}, applying a mapping function to each data element.
     *
     * @param <T>      the source entity type
     * @param <R>      the target DTO/projection type
     * @param data     reactive stream of source entities
     * @param count    reactive count of total matching entities
     * @param mapper   function to transform each source entity into the target type
     * @param pageable pagination parameters (page number, size, sort)
     * @return a {@link Mono} emitting the assembled {@link Page}
     */
    public static <T,R> Mono<Page<R>> toPage(
            Flux<T> data, Mono<Long> count, Function<T, R> mapper, Pageable pageable){

        return data.map(mapper)
                .collectList()
                .zipWith(count)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

}

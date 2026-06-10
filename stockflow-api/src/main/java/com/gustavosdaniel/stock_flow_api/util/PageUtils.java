package com.gustavosdaniel.stock_flow_api.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class PageUtils {

    private PageUtils(){
        throw new IllegalStateException("Utility class");
    }

    public static <T,R> Mono<Page<R>> toPage(
            Flux<T> data, Mono<Long> count, Function<T, R> mapper, Pageable pageable){

        return data.map(mapper)
                .collectList()
                .zipWith(count)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

}

package com.gustavosdaniel.stock_flow_api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class ReactorContextConfig {

    @PostConstruct
    public void init(){

        Hooks.enableAutomaticContextPropagation();
    }
}

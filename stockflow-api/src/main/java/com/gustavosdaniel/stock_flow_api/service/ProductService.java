package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import com.gustavosdaniel.stock_flow_api.util.SkuGenerator;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private Mono<String> generateUniqueSku(
            String categoryName,
            String supplierName,
            String productName
    ){
        return Mono.defer(() -> {

            String sku = SkuGenerator.generate(categoryName, supplierName, productName);

            return productRepository.existsBySku(sku)
                    .flatMap(exists -> {
                        if (exists){

                            log.warn("SKU {} já existe, gerando novo...", sku);
                            return generateUniqueSku(categoryName, supplierName, productName);
                        }
                        return Mono.just(sku);
                    });
        });
    }
}

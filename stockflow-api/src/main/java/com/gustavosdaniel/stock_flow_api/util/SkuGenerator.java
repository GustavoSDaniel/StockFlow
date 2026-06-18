package com.gustavosdaniel.stock_flow_api.util;

import java.text.Normalizer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class SkuGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private SkuGenerator(){}

    public static String generate(
            String categoryName,
            String supplierName,
            String productName
    ){
        String category = sanitize(categoryName, 4);
        String supplier = sanitize(supplierName, 4);
        String product = sanitize(productName, 4);
        String random = randomAlphanumeric(4);

        int currentCount = COUNTER.getAndUpdate(n -> (n + 1) % 10000);
        String counter = String.format("%04d", currentCount);

        return String.format("%s-%s-%s-%s-%s",
                category, supplier, product, random, counter);
    }

    private static String sanitize(String value, int length){

        if (value == null || value.isBlank()) return "XXX";

        String sanitized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();
        return sanitized.substring(0, Math.min(length, sanitized.length()));
    }

    private static String randomAlphanumeric(int length) {

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        return ThreadLocalRandom.current()
                .ints(length, 0, chars.length())
                .mapToObj(chars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}

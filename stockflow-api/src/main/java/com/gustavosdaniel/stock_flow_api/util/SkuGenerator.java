package com.gustavosdaniel.stock_flow_api.util;

import java.text.Normalizer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique, human-readable SKU (Stock Keeping Unit) codes for products.
 * <p>
 * The SKU format is:
 * {@code CCCC-SSSS-PPPP-RRRR-NNNN}, where:
 * <ul>
 *   <li>{@code CCCC} — first 4 sanitized characters of the category name</li>
 *   <li>{@code SSSS} — first 4 sanitized characters of the supplier name</li>
 *   <li>{@code PPPP} — first 4 sanitized characters of the product name</li>
 *   <li>{@code RRRR} — 4 random alphanumeric characters</li>
 *   <li>{@code NNNN} — sequential counter (0-9999, wraps around)</li>
 * </ul>
 * Sanitization removes accents and non-alphanumeric characters, converting
 * to uppercase.
 * </p>
 */
public class SkuGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private SkuGenerator(){}

    /**
     * Generates a SKU based on category, supplier, and product names.
     *
     * @param categoryName the product's category name
     * @param supplierName the product's supplier name
     * @param productName  the product name
     * @return a formatted SKU string (e.g. {@code ELET-SAMS-TVSM-A7K2-0042})
     */
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

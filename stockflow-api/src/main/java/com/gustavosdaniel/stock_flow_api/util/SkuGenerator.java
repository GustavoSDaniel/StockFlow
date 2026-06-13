package com.gustavosdaniel.stock_flow_api.util;

import java.text.Normalizer;
import java.util.Random;

public class SkuGenerator {

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

        return String.format("%s-%s-%s-%s",
                category, supplier, product, random);
    }

    private static String sanitize(String value, int length){

        if (value == null || value.isBlank()) return "XXX";

        String sanitized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();
        return sanitized.substring(0, Math.min(length, value.length()));
    }

    private static String randomAlphanumeric(int length) {

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            stringBuilder.append(chars.charAt(random.nextInt(chars.length())));
        }

        return stringBuilder.toString();
    }
}

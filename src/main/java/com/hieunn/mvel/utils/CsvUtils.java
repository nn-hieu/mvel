package com.hieunn.mvel.utils;

import org.springframework.stereotype.Component;

@Component
public class CsvUtils {
    public String normalizeHeader(String header) {
        return header
                .trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "_");
    }
}

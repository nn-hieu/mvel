package com.hieunn.mvel.utils;

import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class DataTypeUtils {
    public Object autoParse(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        if ("true".equalsIgnoreCase(value)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(value)) return Boolean.FALSE;

        if (value.matches("-?\\d+")) {
            if (value.length() <= 18) {
                return Long.parseLong(value);
            } else {
                return new BigInteger(value);
            }
        }

        if (value.matches("-?\\d+\\.\\d+")) {
            return Double.parseDouble(value);
        }

        return value;
    }

    public Object parseValueByType(String value, String type) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            switch (type.trim().toLowerCase()) {
                case "integer":
                case "int":
                    return Integer.parseInt(value);

                case "long":
                    return Long.parseLong(value);

                case "bigint":
                case "biginteger":
                    return new BigInteger(value);

                case "double":
                case "float":
                    return Double.parseDouble(value);

                case "boolean":
                    if ("true".equalsIgnoreCase(value)) return Boolean.TRUE;
                    if ("false".equalsIgnoreCase(value)) return Boolean.FALSE;
                    throw new IllegalArgumentException("Invalid boolean: " + value);

                case "string":
                    return value;

                default:
                    return this.autoParse(value);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

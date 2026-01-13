package com.hieunn.mvel.mvel;

import com.hieunn.mvel.utils.DataTypeUtils;
import org.apache.commons.csv.CSVRecord;

import java.util.HashMap;
import java.util.Map;

public class LazyCsvContext extends HashMap<String, Object> {
    private final CSVRecord record;
    private final Map<String, Integer> headerIndexMap;
    private final Map<String, String> columnDataType;
    private final DataTypeUtils dataTypeUtils;
    private final Map<String, Object> parsedCache = new HashMap<>();

    public LazyCsvContext(
            CSVRecord record,
            Map<String, Integer> headerIndexMap,
            Map<String, String> columnDataType,
            DataTypeUtils dataTypeUtils
    ) {
        this.record = record;
        this.headerIndexMap = headerIndexMap;
        this.columnDataType = columnDataType;
        this.dataTypeUtils = dataTypeUtils;
    }

    @Override
    public Object get(Object key) {
        String k = (String) key;

        if (parsedCache.containsKey(k)) {
            return parsedCache.get(k);
        }

        Integer index = headerIndexMap.get(k);
        if (index == null) {
            return null;
        }

        String rawValue = record.get(index);

        Object parsedValue;
        if (columnDataType.containsKey(k)) {
            parsedValue = dataTypeUtils.parseValueByType(rawValue, columnDataType.get(k));
        } else {
            parsedValue = dataTypeUtils.autoParse(rawValue);
        }

        parsedCache.put(k, parsedValue);
        return parsedValue;
    }

    @Override
    public boolean containsKey(Object key) {
        return headerIndexMap.containsKey(key);
    }
}

package com.hieunn.mvel.mvel;

import com.hieunn.mvel.utils.DataTypeUtils;
import org.apache.commons.csv.CSVRecord;

import java.util.HashMap;
import java.util.Map;

public class LazyCsvContext extends HashMap<String, Object> {
    private CSVRecord record;
    private final Map<String, Integer> headerIndexMap;
    private final Map<String, String> columnDataType;
    private final DataTypeUtils dataTypeUtils;
    private final Map<String, Object> parsedCache = new HashMap<>();

    public LazyCsvContext(
            Map<String, Integer> headerIndexMap,
            Map<String, String> columnDataType,
            DataTypeUtils dataTypeUtils
    ) {
        this.headerIndexMap = headerIndexMap;
        this.columnDataType = columnDataType;
        this.dataTypeUtils = dataTypeUtils;
    }

    public void setRecord(CSVRecord record) {
        this.record = record;
        this.parsedCache.clear();
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String k)) {
            return null;
        }

        Object cached = parsedCache.get(k);
        if (cached != null || parsedCache.containsKey(k)) {
            return cached;
        }

        Integer index = headerIndexMap.get(k);
        if (index == null) {
            return null;
        }

        String rawValue = record.get(index);

        String type = columnDataType.get(k);
        Object parsedValue = (type != null)
                ? dataTypeUtils.parseValueByType(rawValue, type)
                : dataTypeUtils.autoParse(rawValue);

        parsedCache.put(k, parsedValue);
        return parsedValue;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && headerIndexMap.containsKey(key);
    }
}

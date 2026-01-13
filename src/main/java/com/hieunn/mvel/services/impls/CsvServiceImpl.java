package com.hieunn.mvel.services.impls;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hieunn.mvel.services.CsvService;
import com.hieunn.mvel.utils.CsvUtils;
import com.hieunn.mvel.utils.DataTypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.mvel2.MVEL;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvServiceImpl implements CsvService {
    private final DataTypeUtils dataTypeUtils;
    private final CsvUtils csvUtils;
    private final ObjectMapper objectMapper;

    @Override
    public byte[] filterCsv(MultipartFile file, MultipartFile dataType, String mvelExpression, String delimiter) throws IOException {
        Map<String, String> columnDataType = new HashMap<>();
        if (dataType != null && !dataType.isEmpty()) {
            try {
                columnDataType = objectMapper.readValue(dataType.getInputStream(), new TypeReference<>() {});
            } catch (Exception e) {
                log.error("Invalid json file, using auto-parse", e);
            }
        }

        CSVFormat readFormat = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .setAllowMissingColumnNames(false)
                .get();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = file.getInputStream();

        try (
                Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                CSVParser parser = readFormat.parse(reader);
                Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {
            Serializable compiledExpression = MVEL.compileExpression(mvelExpression);

            List<String> originalHeaders = parser.getHeaderNames();
            printer.printRecord(originalHeaders);

            Map<String, String> headerMapping = new LinkedHashMap<>();
            for (String h : originalHeaders) {
                headerMapping.put(h, csvUtils.normalizeHeader(h));
            }

            Map<String, Object> context = new HashMap<>();

            for (CSVRecord record : parser) {
                context.clear();

                for (Map.Entry<String, String> entry : headerMapping.entrySet()) {
                    String originalHeader = entry.getKey();
                    String normalizedHeader = entry.getValue();
                    String rawValue = record.get(originalHeader);

                    Object parsedValue;
                    if (columnDataType.containsKey(originalHeader)) {
                        String type = columnDataType.get(originalHeader);
                        parsedValue = dataTypeUtils.parseValueByType(rawValue, type);
                    } else {
                        parsedValue = dataTypeUtils.autoParse(rawValue);
                    }

                    context.put(normalizedHeader, parsedValue);
                }

                try {
                    Object matched = MVEL.executeExpression(compiledExpression, context);
                    if (matched instanceof Boolean && (Boolean) matched) {
                        printer.printRecord(record);
                    }
                } catch (Exception e) {
                    log.warn("Error when processing row {}: {}", record.getRecordNumber(), e.getMessage());
                }
            }

            printer.flush();
        }

        return outputStream.toByteArray();
    }
}

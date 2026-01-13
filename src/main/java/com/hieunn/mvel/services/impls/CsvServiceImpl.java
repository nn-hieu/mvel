package com.hieunn.mvel.services.impls;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hieunn.mvel.mvel.LazyCsvContext;
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
import org.mvel2.ParserContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvServiceImpl implements CsvService {
    private final DataTypeUtils dataTypeUtils;
    private final CsvUtils csvUtils;
    private final ObjectMapper objectMapper;

    @Override
    public byte[] filterCsv(MultipartFile file, MultipartFile dataType, String mvelExpression, String delimiter) throws IOException {
        Map<String, String> rawColumnToType = new HashMap<>();
        if (dataType != null && !dataType.isEmpty()) {
            try {
                rawColumnToType = objectMapper.readValue(dataType.getInputStream(), new TypeReference<>() {
                });
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
        AtomicBoolean isFilteringFinished = new AtomicBoolean(false);
        Queue<CSVRecord> queue = new ConcurrentLinkedQueue<>();
        try (
                Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = readFormat.parse(reader);
                Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {
            Serializable compiledExpression = MVEL.compileExpression(mvelExpression);

            List<String> originalHeaders = parser.getHeaderNames();
            printer.printRecord(originalHeaders);

            Map<String, Integer> headerIndexMap = new HashMap<>();
            Map<String, String> normalizedColumnToType = new HashMap<>();

            for (int i = 0; i < originalHeaders.size(); i++) {
                String originalHeader = originalHeaders.get(i);
                String normalizedHeader = csvUtils.normalizeHeader(originalHeader);
                headerIndexMap.put(normalizedHeader, i);

                if (rawColumnToType.containsKey(originalHeader)) {
                    normalizedColumnToType.put(normalizedHeader, rawColumnToType.get(originalHeader));
                }
            }

            CompletableFuture<Void> writeCsvTask = CompletableFuture.runAsync(() -> {
                try {
                    while (!isFilteringFinished.get() || !queue.isEmpty()) {
                        CSVRecord record = queue.poll();
                        if (record != null) {
                            printer.printRecord(record);
                        } else {
                            Thread.onSpinWait();
                        }
                    }
                } catch (IOException e) {
                    log.error("Error while writing CSV file", e);
                    throw new RuntimeException(e);
                }
            });

            StreamSupport.stream(parser.spliterator(), true)
                    .forEach(record -> {
                        Map<String, Object> context = new LazyCsvContext(
                                record,
                                headerIndexMap,
                                normalizedColumnToType,
                                dataTypeUtils
                        );

                        Object matched = MVEL.executeExpression(compiledExpression, context);
                        if (matched instanceof Boolean && (Boolean) matched) {
                            queue.add(record);
                        }
                    });
            isFilteringFinished.set(true);

            writeCsvTask.join();

            printer.flush();
        }

        return outputStream.toByteArray();
    }
}

package com.hieunn.mvel.controllers;

import com.hieunn.mvel.services.CsvService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/csv")
@RequiredArgsConstructor
public class CsvController {
    private final CsvService csvService;

    @PostMapping(value = "/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> filterCsvFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "dataType", required = false) MultipartFile dataType,
            @RequestParam("expression") String expression,
            @RequestParam(value = "delimiter", required = false, defaultValue = ";") String delimiter
    ) {
        try {
            byte[] filteredData = csvService.filterCsv(file, dataType, expression, delimiter);

            ByteArrayResource resource = new ByteArrayResource(filteredData);

            String outputFileName = "filtered_" + file.getOriginalFilename();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFileName + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

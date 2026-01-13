package com.hieunn.mvel.controllers;

import com.hieunn.mvel.dtos.CsvFilterDto;
import com.hieunn.mvel.services.CsvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/csv")
@RequiredArgsConstructor
public class CsvController {
    private final CsvService csvService;

    @PostMapping(value = "/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Filter CSV File",
            description = """
                Upload your CSV file and filter it by using MVEL expression.
                You will get a link to download a filtered CSV file after called API.
                """,
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "csvFilterDto", contentType = MediaType.APPLICATION_JSON_VALUE)
                            }
                    )
            )
    )
    public ResponseEntity<Resource> filterCsvFile(
            @RequestPart("csvFile")
            @Parameter(
                    description = "CSV file",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            MultipartFile csvFile,

            @RequestPart(value = "dataType", required = false)
            @Parameter(
                    description = "Json file for describing each column data type (optional)",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            MultipartFile dataType,

            @RequestPart
            @Parameter(
                    description = "DTO for storing expression and delimiter (default using comma) of CSV",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CsvFilterDto.class)
                    )
            )
            CsvFilterDto csvFilterDto
    ) {
        try {
            byte[] filteredData = csvService.filterCsv(
                    csvFile,
                    dataType,
                    csvFilterDto.getExpression(),
                    csvFilterDto.getDelimiter()
            );

            ByteArrayResource resource = new ByteArrayResource(filteredData);

            String outputFileName = "filtered_" + csvFile.getOriginalFilename();

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

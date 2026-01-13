package com.hieunn.mvel.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CsvService {
    byte[] filterCsv(MultipartFile file, MultipartFile dataType, String mvelExpression, String delimiter) throws IOException;
}

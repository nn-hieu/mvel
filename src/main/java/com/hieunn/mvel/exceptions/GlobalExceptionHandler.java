package com.hieunn.mvel.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e) {
        log.error(e.getMessage());

        if (e.getMessage().contains("uncomparable values")) {
            return ResponseEntity
                    .internalServerError()
                    .body("Invalid comparison due to mismatched data types");
        }

        if (e.getMessage().contains("unresolvable property")) {
            String unknownVar = this.extractUnknownVariable(e.getMessage());

            return ResponseEntity
                    .internalServerError()
                    .body("Unknown variable '" + unknownVar + "' in expression. The column does not exist in the CSV header.");
        }

        return ResponseEntity
                .internalServerError()
                .body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        log.error(e.getMessage());
        return ResponseEntity
                .internalServerError()
                .body(e.getMessage());
    }

    private String extractUnknownVariable(String message) {
        Pattern pattern = Pattern.compile("unresolvable property or identifier:\\s*([A-Za-z_][A-Za-z0-9_]*)");
        if (message == null) return null;

        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}

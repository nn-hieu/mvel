package com.hieunn.mvel.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CsvFilterDto {
    private String expression;

    @Schema(defaultValue = ",")
    private String delimiter = ",";
}

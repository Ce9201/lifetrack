package com.sun.lifetrack.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SleepRecordRequest {
    @Schema(description = "睡眠日期", example = "2024-03-11")
    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @Schema(description = "入睡时间", example = "2024-03-11T22:00:00")
    @NotNull(message = "入睡时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "醒来时间", example = "2024-03-12T07:30:00")
    @NotNull(message = "醒来时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "备注", example = "睡得不错")
    @Size(max = 200, message = "备注不能超过200个字符")
    private String notes;
}

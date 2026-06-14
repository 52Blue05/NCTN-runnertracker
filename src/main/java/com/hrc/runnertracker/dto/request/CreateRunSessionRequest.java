package com.hrc.runnertracker.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRunSessionRequest {

    @NotNull(message = "start_time không được để trống")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "distance_km không được để trống")
    @Min(value = 0, message = "distance_km phải >= 0")
    private BigDecimal distanceKm;

    @NotNull(message = "duration_seconds không được để trống")
    @Min(value = 0, message = "duration_seconds phải >= 0")
    private Integer durationSeconds;

    @Min(value = 0, message = "step_count phải >= 0")
    private Integer stepCount;

    /**
     * JSON array tọa độ — ví dụ: [[21.028511, 105.804817], [21.029, 105.805]]
     * FE gửi dưới dạng JSON string, BE lưu nguyên vào DB.
     */
    private String polylineData;

    private String status;
}

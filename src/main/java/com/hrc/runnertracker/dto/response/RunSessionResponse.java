package com.hrc.runnertracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunSessionResponse {

    private Long id;
    private Long userId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BigDecimal distanceKm;
    private Integer durationSeconds;
    private BigDecimal avgPace;

    private Integer stepCount;

    /**
     * JSON array tọa độ — FE parse thành List<LatLng>
     */
    private String polylineData;

    private String status;
    private LocalDateTime createdAt;
}

package com.hrc.runnertracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {

    private int rank;
    private Long userId;
    private String fullName;
    private BigDecimal totalDistanceKm;
    private long runCount;
}

package com.hrc.runnertracker.controller;

import com.hrc.runnertracker.dto.response.ApiResponse;
import com.hrc.runnertracker.dto.response.LeaderboardEntry;
import com.hrc.runnertracker.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * GET /api/v1/leaderboard?period=weekly|monthly
     * Trả về bảng xếp hạng theo tổng quãng đường.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getLeaderboard(
            @RequestParam(defaultValue = "weekly") String period) {

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(period);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy bảng xếp hạng thành công", leaderboard));
    }
}

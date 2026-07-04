package com.hrc.runnertracker.service;

import com.hrc.runnertracker.dto.response.LeaderboardEntry;
import com.hrc.runnertracker.repository.RunSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final RunSessionRepository runSessionRepository;

    /**
     * Lấy bảng xếp hạng theo period (weekly / monthly).
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(String period) {
        LocalDateTime from;
        LocalDateTime to;

        LocalDate today = LocalDate.now();

        if ("monthly".equalsIgnoreCase(period)) {
            // Từ ngày 1 đầu tháng đến ngày 1 tháng sau
            LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate firstDayOfNextMonth = today.with(TemporalAdjusters.firstDayOfNextMonth());
            from = firstDayOfMonth.atStartOfDay();
            to = firstDayOfNextMonth.atStartOfDay();
        } else {
            // Default: weekly — từ thứ 2 đầu tuần đến thứ 2 tuần sau
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate nextMonday = monday.plusWeeks(1);
            from = monday.atStartOfDay();
            to = nextMonday.atStartOfDay();
        }

        List<Object[]> rows = runSessionRepository.findLeaderboard(from, to);

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            LeaderboardEntry entry = LeaderboardEntry.builder()
                    .rank(rank++)
                    .userId((Long) row[0])
                    .fullName((String) row[1])
                    .totalDistanceKm((BigDecimal) row[2])
                    .runCount((Long) row[3])
                    .build();
            leaderboard.add(entry);
        }
        return leaderboard;
    }
}

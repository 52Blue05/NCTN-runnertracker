package com.hrc.runnertracker.service;

import com.hrc.runnertracker.dto.request.CreateRunSessionRequest;
import com.hrc.runnertracker.dto.response.RunSessionResponse;
import com.hrc.runnertracker.entity.RunSession;
import com.hrc.runnertracker.entity.User;
import com.hrc.runnertracker.exception.ResourceNotFoundException;
import com.hrc.runnertracker.repository.RunSessionRepository;
import com.hrc.runnertracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class RunSessionService {

    private final RunSessionRepository runSessionRepository;
    private final UserRepository userRepository;

    /**
     * Tạo run session mới cho user đang đăng nhập.
     * Tự động tính avg_pace = duration_seconds / 60 / distance_km (phút/km)
     */
    @Transactional
    public RunSessionResponse createRunSession(String username, CreateRunSessionRequest request) {
        User user = findUserByUsername(username);

        // Tính avg_pace (phút/km)
        BigDecimal avgPace = calculateAvgPace(request.getDurationSeconds(), request.getDistanceKm());

        // Parse status, default COMPLETED
        RunSession.Status status = RunSession.Status.COMPLETED;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = RunSession.Status.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Status không hợp lệ. Chỉ chấp nhận: ONGOING, COMPLETED, CANCELED");
            }
        }

        RunSession runSession = RunSession.builder()
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .distanceKm(request.getDistanceKm())
                .durationSeconds(request.getDurationSeconds())
                .avgPace(avgPace)
                .stepCount(request.getStepCount() != null ? request.getStepCount() : 0)
                .polylineData(request.getPolylineData())
                .status(status)
                .build();

        RunSession saved = runSessionRepository.save(runSession);
        return toResponse(saved);
    }

    /**
     * Lấy danh sách run sessions của user đang đăng nhập (phân trang).
     */
    @Transactional(readOnly = true)
    public Page<RunSessionResponse> getRunSessions(String username, Pageable pageable) {
        User user = findUserByUsername(username);
        Page<RunSession> page = runSessionRepository.findByUserIdOrderByStartTimeDesc(user.getId(), pageable);
        return page.map(this::toResponse);
    }

    /**
     * Lấy chi tiết 1 run session — chỉ cho phép user xem session của mình.
     */
    @Transactional(readOnly = true)
    public RunSessionResponse getRunSessionById(String username, Long runId) {
        User user = findUserByUsername(username);
        RunSession runSession = runSessionRepository.findByIdAndUserId(runId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RunSession", "id", runId));
        return toResponse(runSession);
    }

    /**
     * Tính avg_pace = duration_seconds / 60 / distance_km (phút/km).
     * Trả null nếu distance = 0 hoặc duration = 0.
     */
    private BigDecimal calculateAvgPace(Integer durationSeconds, BigDecimal distanceKm) {
        if (durationSeconds == null || durationSeconds == 0
                || distanceKm == null || distanceKm.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // durationMinutes = durationSeconds / 60
        BigDecimal durationMinutes = BigDecimal.valueOf(durationSeconds)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        // avgPace = durationMinutes / distanceKm
        return durationMinutes.divide(distanceKm, 2, RoundingMode.HALF_UP);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
    }

    private RunSessionResponse toResponse(RunSession rs) {
        return RunSessionResponse.builder()
                .id(rs.getId())
                .userId(rs.getUser().getId())
                .startTime(rs.getStartTime())
                .endTime(rs.getEndTime())
                .distanceKm(rs.getDistanceKm())
                .durationSeconds(rs.getDurationSeconds())
                .avgPace(rs.getAvgPace())
                .stepCount(rs.getStepCount())
                .polylineData(rs.getPolylineData())
                .status(rs.getStatus().name())
                .createdAt(rs.getCreatedAt())
                .build();
    }
}

package com.hrc.runnertracker.controller;

import com.hrc.runnertracker.dto.request.CreateRunSessionRequest;
import com.hrc.runnertracker.dto.response.ApiResponse;
import com.hrc.runnertracker.dto.response.RunSessionResponse;
import com.hrc.runnertracker.service.RunSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/runs")
@RequiredArgsConstructor
public class RunSessionController {

    private final RunSessionService runSessionService;

    /**
     * POST /api/v1/runs — Lưu 1 buổi chạy mới.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RunSessionResponse>> createRunSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateRunSessionRequest request) {

        RunSessionResponse response = runSessionService.createRunSession(
                userDetails.getUsername(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lưu buổi chạy thành công", response));
    }

    /**
     * GET /api/v1/runs — Lấy danh sách buổi chạy của user đang đăng nhập (phân trang).
     * Query params: page (default 0), size (default 10)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RunSessionResponse>>> getRunSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<RunSessionResponse> runs = runSessionService.getRunSessions(
                userDetails.getUsername(), pageable);

        return ResponseEntity
                .ok(ApiResponse.success("Lấy danh sách buổi chạy thành công", runs));
    }

    /**
     * GET /api/v1/runs/{id} — Lấy chi tiết 1 buổi chạy.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RunSessionResponse>> getRunSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        RunSessionResponse response = runSessionService.getRunSessionById(
                userDetails.getUsername(), id);

        return ResponseEntity
                .ok(ApiResponse.success("Lấy chi tiết buổi chạy thành công", response));
    }
}

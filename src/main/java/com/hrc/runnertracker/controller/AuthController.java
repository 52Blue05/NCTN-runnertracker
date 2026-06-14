package com.hrc.runnertracker.controller;

import com.hrc.runnertracker.dto.request.LoginRequest;
import com.hrc.runnertracker.dto.request.RegisterRequest;
import com.hrc.runnertracker.dto.response.ApiResponse;
import com.hrc.runnertracker.dto.response.AuthResponse;
import com.hrc.runnertracker.dto.response.UserProfileResponse;
import com.hrc.runnertracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Đăng ký tài khoản mới.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserProfileResponse profile = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", profile));
    }

    /**
     * POST /api/v1/auth/login
     * Đăng nhập, trả về JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity
                .ok(ApiResponse.success("Đăng nhập thành công", authResponse));
    }
}

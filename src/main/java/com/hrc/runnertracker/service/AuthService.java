package com.hrc.runnertracker.service;

import com.hrc.runnertracker.dto.request.LoginRequest;
import com.hrc.runnertracker.dto.request.RegisterRequest;
import com.hrc.runnertracker.dto.response.AuthResponse;
import com.hrc.runnertracker.dto.response.UserProfileResponse;
import com.hrc.runnertracker.entity.User;
import com.hrc.runnertracker.repository.UserRepository;
import com.hrc.runnertracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Đăng ký user mới.
     * - Kiểm tra trùng username/email
     * - BCrypt mã hóa password
     * - Lưu user vào DB
     * - Trả về UserProfileResponse
     */
    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        // Validate trùng username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' đã được sử dụng");
        }

        // Validate trùng email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        // Tạo entity User
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .weight(request.getWeight())
                .height(request.getHeight())
                .build();

        User savedUser = userRepository.save(user);

        return toUserProfileResponse(savedUser);
    }

    /**
     * Đăng nhập user.
     * - Authenticate bằng Spring Security AuthenticationManager
     * - Generate JWT token
     * - Trả về AuthResponse
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate — sẽ throw BadCredentialsException nếu sai
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Lấy user từ DB để trả thêm info
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate token
        String token = jwtTokenProvider.generateToken(authentication.getName());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Map User entity → UserProfileResponse DTO
     */
    private UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .weight(user.getWeight())
                .height(user.getHeight())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

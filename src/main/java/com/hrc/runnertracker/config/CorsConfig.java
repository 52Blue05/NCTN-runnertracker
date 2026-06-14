package com.hrc.runnertracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Cấu hình CORS cho phép Flutter app (mobile & web) gọi API.
 * Đặt ở đây để không bị quên khi thêm endpoint mới.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép tất cả origin — phù hợp với Flutter mobile
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));

        // Headers cho phép
        config.setAllowedHeaders(List.of("*"));

        // HTTP methods cho phép
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Expose Authorization header để Flutter đọc được
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Preflight cache — 1 giờ
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

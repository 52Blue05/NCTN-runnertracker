package com.hrc.runnertracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "run_sessions", indexes = {
        @Index(name = "idx_run_user", columnList = "user_id, start_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "distance_km", precision = 8, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "avg_pace", precision = 5, scale = 2)
    private BigDecimal avgPace;

    @Column(name = "step_count")
    @Builder.Default
    private Integer stepCount = 0;

    @Column(name = "polyline_data", columnDefinition = "LONGTEXT")
    private String polylineData;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('ONGOING','COMPLETED','CANCELED') DEFAULT 'COMPLETED'")
    @Builder.Default
    private Status status = Status.COMPLETED;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public enum Status {
        ONGOING,
        COMPLETED,
        CANCELED
    }
}

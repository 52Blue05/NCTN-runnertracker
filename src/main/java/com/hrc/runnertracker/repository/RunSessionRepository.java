package com.hrc.runnertracker.repository;

import com.hrc.runnertracker.entity.RunSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RunSessionRepository extends JpaRepository<RunSession, Long> {

    /**
     * Lấy danh sách run sessions của 1 user, phân trang, sắp xếp theo startTime DESC.
     */
    Page<RunSession> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);

    /**
     * Tìm run session theo id và user id — đảm bảo user chỉ xem được session của mình.
     */
    Optional<RunSession> findByIdAndUserId(Long id, Long userId);

    /**
     * Leaderboard: tổng hợp quãng đường theo user trong khoảng thời gian.
     * Trả về [userId, fullName, totalDistanceKm, runCount].
     */
    @Query("SELECT r.user.id, r.user.fullName, SUM(r.distanceKm), COUNT(r) " +
           "FROM RunSession r " +
           "WHERE r.startTime >= :fromDate AND r.startTime < :toDate " +
           "  AND r.status = com.hrc.runnertracker.entity.RunSession.Status.COMPLETED " +
           "GROUP BY r.user.id, r.user.fullName " +
           "ORDER BY SUM(r.distanceKm) DESC")
    List<Object[]> findLeaderboard(@Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate);
}


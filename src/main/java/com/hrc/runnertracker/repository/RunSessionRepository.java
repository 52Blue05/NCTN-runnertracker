package com.hrc.runnertracker.repository;

import com.hrc.runnertracker.entity.RunSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}

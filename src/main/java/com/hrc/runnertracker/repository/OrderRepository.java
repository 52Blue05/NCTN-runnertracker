package com.hrc.runnertracker.repository;

import com.hrc.runnertracker.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Lấy danh sách đơn hàng của user, sắp xếp mới nhất trước.
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Tìm đơn hàng theo id và user id — đảm bảo user chỉ xem đơn của mình.
     */
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}

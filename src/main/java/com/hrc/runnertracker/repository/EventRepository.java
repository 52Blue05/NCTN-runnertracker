package com.hrc.runnertracker.repository;

import com.hrc.runnertracker.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Lấy các sự kiện sắp tới (eventDate > now), sắp xếp theo ngày gần nhất.
     */
    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime now);

    /**
     * Lấy tất cả sự kiện, sắp xếp theo ngày.
     */
    List<Event> findAllByOrderByEventDateDesc();
}

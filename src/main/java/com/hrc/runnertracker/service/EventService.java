package com.hrc.runnertracker.service;

import com.hrc.runnertracker.dto.request.CreateEventRequest;
import com.hrc.runnertracker.dto.response.EventResponse;
import com.hrc.runnertracker.entity.Event;
import com.hrc.runnertracker.exception.ResourceNotFoundException;
import com.hrc.runnertracker.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Lấy danh sách sự kiện sắp tới (upcoming) — có phân trang.
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getUpcomingEvents(Pageable pageable) {
        Page<Event> page = eventRepository.findByEventDateAfterOrderByEventDateAsc(LocalDateTime.now(), pageable);
        return page.map(this::toResponse);
    }

    /**
     * Lấy tất cả sự kiện — có phân trang.
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        Page<Event> page = eventRepository.findAllByOrderByEventDateDesc(pageable);
        return page.map(this::toResponse);
    }

    /**
     * Lấy chi tiết sự kiện theo ID.
     */
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        return toResponse(event);
    }

    /**
     * Tạo sự kiện mới (chỉ ADMIN).
     */
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .location(request.getLocation())
                .eventDate(request.getEventDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .maxParticipants(request.getMaxParticipants())
                .status(Event.EventStatus.UPCOMING)
                .build();

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .location(event.getLocation())
                .eventDate(event.getEventDate())
                .registrationDeadline(event.getRegistrationDeadline())
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus().name())
                .createdAt(event.getCreatedAt())
                .build();
    }
}


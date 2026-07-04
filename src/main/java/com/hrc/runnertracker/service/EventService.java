package com.hrc.runnertracker.service;

import com.hrc.runnertracker.dto.request.CreateEventRequest;
import com.hrc.runnertracker.dto.response.EventResponse;
import com.hrc.runnertracker.entity.Event;
import com.hrc.runnertracker.exception.ResourceNotFoundException;
import com.hrc.runnertracker.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Lấy danh sách sự kiện sắp tới (upcoming).
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents() {
        List<Event> events = eventRepository.findByEventDateAfterOrderByEventDateAsc(LocalDateTime.now());
        return events.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Lấy tất cả sự kiện.
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAllByOrderByEventDateDesc();
        return events.stream().map(this::toResponse).collect(Collectors.toList());
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

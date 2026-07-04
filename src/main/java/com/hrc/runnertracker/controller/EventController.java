package com.hrc.runnertracker.controller;

import com.hrc.runnertracker.dto.request.CreateEventRequest;
import com.hrc.runnertracker.dto.response.ApiResponse;
import com.hrc.runnertracker.dto.response.EventResponse;
import com.hrc.runnertracker.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * GET /api/v1/events — Lấy danh sách sự kiện.
     * Query param: filter=upcoming (default) hoặc filter=all
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getEvents(
            @RequestParam(defaultValue = "upcoming") String filter) {

        List<EventResponse> events;
        if ("all".equalsIgnoreCase(filter)) {
            events = eventService.getAllEvents();
        } else {
            events = eventService.getUpcomingEvents();
        }

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách sự kiện thành công", events));
    }

    /**
     * GET /api/v1/events/{id} — Chi tiết sự kiện.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy chi tiết sự kiện thành công", event));
    }

    /**
     * POST /api/v1/events — Tạo sự kiện mới (chỉ ADMIN).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        EventResponse response = eventService.createEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo sự kiện thành công", response));
    }
}

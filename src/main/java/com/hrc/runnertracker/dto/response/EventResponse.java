package com.hrc.runnertracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String location;
    private LocalDateTime eventDate;
    private LocalDateTime registrationDeadline;
    private Integer maxParticipants;
    private String status;
    private LocalDateTime createdAt;
}

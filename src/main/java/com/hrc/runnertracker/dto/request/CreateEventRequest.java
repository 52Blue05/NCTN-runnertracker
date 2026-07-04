package com.hrc.runnertracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Tên sự kiện không được để trống")
    private String title;

    private String description;

    private String imageUrl;

    private String location;

    @NotNull(message = "Ngày tổ chức không được để trống")
    private LocalDateTime eventDate;

    private LocalDateTime registrationDeadline;

    private Integer maxParticipants;
}

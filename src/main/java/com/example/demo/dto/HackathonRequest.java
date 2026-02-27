package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO for creating or updating a Hackathon.
 *
 * FIX (M3): Added @FutureOrPresent on startDate so organizers cannot
 * create hackathons with dates in the past.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HackathonRequest {

    @NotBlank(message = "Hackathon name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Min(value = 1, message = "Max participants must be at least 1")
    @Max(value = 100000, message = "Max participants cannot exceed 100,000")
    @Builder.Default
    private int maxParticipants = 100;

    @DecimalMin(value = "0.0", message = "Prize amount cannot be negative")
    @Builder.Default
    private Double prizeAmount = 0.0;
}

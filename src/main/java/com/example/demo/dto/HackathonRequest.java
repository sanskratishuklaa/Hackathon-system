package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HackathonRequest {

    @NotBlank(message = "Hackathon name is required")
    @Size(min = 3, max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Min(value = 1, message = "Max participants must be at least 1")
    @Builder.Default
    private int maxParticipants = 100;

    @DecimalMin(value = "0.0")
    @Builder.Default
    private Double prizeAmount = 0.0;
}

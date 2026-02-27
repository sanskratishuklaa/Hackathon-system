package com.example.demo.dto;

import com.example.demo.model.HackathonStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HackathonResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxParticipants;
    private Double prizeAmount;
    private HackathonStatus status;
    private String organizerName;
    private Long organizerId;
    private long registrationCount;
    private long projectCount;
    private LocalDateTime createdAt;
}

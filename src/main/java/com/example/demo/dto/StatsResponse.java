package com.example.demo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponse {
    private long totalUsers;
    private long totalHackathons;
    private long activeHackathons;
    private long totalProjects;
    private long totalRegistrations;
    private long completedHackathons;
    private long upcomingHackathons;
}

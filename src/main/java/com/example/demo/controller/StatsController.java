package com.example.demo.controller;

import com.example.demo.dto.StatsResponse;
import com.example.demo.model.HackathonStatus;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.HackathonService;
import com.example.demo.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stats Controller — landing page statistics.
 * Fixed: was returning unrelated leaderboard/fatigue/participant data.
 * Now returns actual platform stats for the landing page.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HackathonService hackathonService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RegistrationRepository registrationRepository;

    /**
     * GET /api/stats
     * Platform-wide statistics for landing page. Public endpoint.
     */
    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = StatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalHackathons(hackathonService.countAll())
                .activeHackathons(hackathonService.countByStatus(HackathonStatus.ACTIVE))
                .upcomingHackathons(hackathonService.countByStatus(HackathonStatus.UPCOMING))
                .completedHackathons(hackathonService.countByStatus(HackathonStatus.COMPLETED))
                .totalProjects(projectService.countAllProjects())
                .totalRegistrations(registrationRepository.countTotalRegistrations())
                .build();

        return ResponseEntity.ok(stats);
    }
}
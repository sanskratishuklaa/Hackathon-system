package com.example.demo.controller;

import com.example.demo.dto.HackathonResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.User;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.service.HackathonService;
import com.example.demo.service.ProjectService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard Controller — role-specific dashboards.
 *
 * Fixes applied:
 * - (H2) Admin dashboard now uses UserResponse DTO list instead of raw User
 * entities.
 * - (L2) Removed per-controller @CrossOrigin.
 * - Participant dashboard returns only needed fields (not a full User entity).
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

        @Autowired
        private HackathonService hackathonService;

        @Autowired
        private ProjectService projectService;

        @Autowired
        private UserService userService;

        @Autowired
        private RegistrationRepository registrationRepository;

        /**
         * GET /api/dashboard/participant
         * Participant dashboard — their registrations and projects.
         */
        @GetMapping("/participant")
        @PreAuthorize("hasAnyRole('PARTICIPANT','ADMIN')")
        public ResponseEntity<Map<String, Object>> participantDashboard(
                        @AuthenticationPrincipal UserDetails currentUser) {

                User user = userService.getUserByEmail(currentUser.getUsername());

                Map<String, Object> dashboard = new HashMap<>();
                // FIX (H2): Use explicit DTO mapping, not raw entity
                dashboard.put("user", userService.toUserResponse(user));
                dashboard.put("myProjects", projectService.getMyProjects(currentUser.getUsername()));
                dashboard.put("registrations", registrationRepository.findByUserId(user.getId()));
                dashboard.put("availableHackathons", hackathonService.getAllHackathons());

                return ResponseEntity.ok(dashboard);
        }

        /**
         * GET /api/dashboard/organizer
         * Organizer dashboard — hackathons they created with aggregate stats.
         */
        @GetMapping("/organizer")
        @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
        public ResponseEntity<Map<String, Object>> organizerDashboard(
                        @AuthenticationPrincipal UserDetails currentUser) {

                User user = userService.getUserByEmail(currentUser.getUsername());
                // FIX (C4): Uses organizer's ID, not getAllHackathons()
                List<HackathonResponse> myHackathons = hackathonService.getHackathonsByOrganizer(user.getId());

                Map<String, Object> dashboard = new HashMap<>();
                dashboard.put("user", userService.toUserResponse(user));
                dashboard.put("myHackathons", myHackathons);
                dashboard.put("totalHackathonsCreated", myHackathons.size());
                dashboard.put("totalParticipants",
                                myHackathons.stream().mapToLong(HackathonResponse::getRegistrationCount).sum());
                dashboard.put("totalProjects",
                                myHackathons.stream().mapToLong(HackathonResponse::getProjectCount).sum());

                return ResponseEntity.ok(dashboard);
        }

        /**
         * GET /api/dashboard/admin
         * Admin dashboard — full platform overview.
         * FIX (H2): allUsers now returns List<UserResponse>, not List<User>.
         */
        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> adminDashboard() {
                Map<String, Object> dashboard = new HashMap<>();
                dashboard.put("totalUsers", userService.countActiveUsers());
                dashboard.put("totalHackathons", hackathonService.countAll());
                dashboard.put("totalProjects", projectService.countAllProjects());
                dashboard.put("totalRegistrations", registrationRepository.countTotalRegistrations());
                // FIX (H2): Returns UserResponse DTOs, not raw User entities
                dashboard.put("allUsers", userService.getAllUsers());
                dashboard.put("allHackathons", hackathonService.getAllHackathons());
                dashboard.put("leaderboard", projectService.getLeaderboard());

                return ResponseEntity.ok(dashboard);
        }
}
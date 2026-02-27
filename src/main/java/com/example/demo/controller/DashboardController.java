package com.example.demo.controller;

import com.example.demo.dto.HackathonResponse;
import com.example.demo.model.User;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.UserRepository;
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
 * Fixed: was returning all hackathons/users without security or role
 * distinction.
 * Now has separate endpoints for participant, organizer, and admin dashboards.
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class DashboardController {

        @Autowired
        private HackathonService hackathonService;

        @Autowired
        private ProjectService projectService;

        @Autowired
        private UserService userService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RegistrationRepository registrationRepository;

        /**
         * GET /api/dashboard/participant
         * Participant dashboard — their registrations and project submissions.
         */
        @GetMapping("/participant")
        @PreAuthorize("hasAnyRole('PARTICIPANT','ADMIN')")
        public ResponseEntity<Map<String, Object>> participantDashboard(
                        @AuthenticationPrincipal UserDetails currentUser) {

                User user = userService.getUserByEmail(currentUser.getUsername());

                Map<String, Object> dashboard = new HashMap<>();
                dashboard.put("user", Map.of(
                                "id", user.getId(),
                                "name", user.getName(),
                                "email", user.getEmail(),
                                "college", user.getCollege() != null ? user.getCollege() : "",
                                "role", user.getRole()));
                dashboard.put("myProjects", projectService.getMyProjects(currentUser.getUsername()));
                dashboard.put("registrations", registrationRepository.findByUserId(user.getId()));
                dashboard.put("availableHackathons", hackathonService.getAllHackathons());

                return ResponseEntity.ok(dashboard);
        }

        /**
         * GET /api/dashboard/organizer
         * Organizer dashboard — hackathons they created and their stats.
         */
        @GetMapping("/organizer")
        @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
        public ResponseEntity<Map<String, Object>> organizerDashboard(
                        @AuthenticationPrincipal UserDetails currentUser) {

                User user = userService.getUserByEmail(currentUser.getUsername());
                List<HackathonResponse> myHackathons = hackathonService.getHackathonsByOrganizer(user.getId());

                Map<String, Object> dashboard = new HashMap<>();
                dashboard.put("user", Map.of(
                                "id", user.getId(),
                                "name", user.getName(),
                                "role", user.getRole()));
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
         */
        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Map<String, Object>> adminDashboard() {
                Map<String, Object> dashboard = new HashMap<>();
                dashboard.put("totalUsers", userRepository.count());
                dashboard.put("totalHackathons", hackathonService.countAll());
                dashboard.put("totalProjects", projectService.countAllProjects());
                dashboard.put("totalRegistrations", registrationRepository.countTotalRegistrations());
                dashboard.put("allUsers", userService.getAllUsers());
                dashboard.put("allHackathons", hackathonService.getAllHackathons());
                dashboard.put("leaderboard", projectService.getLeaderboard());

                return ResponseEntity.ok(dashboard);
        }
}
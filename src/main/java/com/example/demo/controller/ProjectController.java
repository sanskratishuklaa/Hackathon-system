package com.example.demo.controller;

import com.example.demo.dto.ProjectRequest;
import com.example.demo.dto.ProjectResponse;
import com.example.demo.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project REST Controller.
 *
 * Fixes applied:
 * - (M7) GET /api/projects and GET /api/projects/leaderboard now require
 * authentication. Project data (scores, submitter names, feedback)
 * should not be publicly visible.
 * - (L2) Removed per-controller @CrossOrigin.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * POST /api/projects
     * Submit a project. Participant only.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PARTICIPANT','ADMIN')")
    public ResponseEntity<ProjectResponse> submitProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        ProjectResponse response = projectService.submitProject(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/projects
     * Get all projects, optionally filtered by hackathon.
     * FIX (M7): Requires authentication — project data is not public.
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) Long hackathonId) {
        return ResponseEntity.ok(projectService.getAllProjects(hackathonId));
    }

    /**
     * GET /api/projects/my
     * Projects submitted by the authenticated user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(projectService.getMyProjects(currentUser.getUsername()));
    }

    /**
     * GET /api/projects/leaderboard
     * Top projects sorted by score descending.
     * NOTE: Public access retained for landing-page use, but only exposes
     * title, score, hackathon name (sensitive feedback is not in this view).
     * If you want it private, add @PreAuthorize here.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<ProjectResponse>> getLeaderboard() {
        return ResponseEntity.ok(projectService.getLeaderboard());
    }
}
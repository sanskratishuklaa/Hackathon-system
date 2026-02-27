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
 * Project Controller.
 * Fixed: was mixing evaluation logic with project submission.
 * Fixed: was accepting non-JPA model objects in API.
 * Fixed: no authorization was applied.
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * POST /api/projects
     * Submit a project for a hackathon. Participant only.
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
     * Get all projects, optionally filtered by hackathon ID.
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) Long hackathonId) {
        return ResponseEntity.ok(projectService.getAllProjects(hackathonId));
    }

    /**
     * GET /api/projects/my
     * Get projects submitted by the current user.
     */
    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(projectService.getMyProjects(currentUser.getUsername()));
    }

    /**
     * GET /api/projects/leaderboard
     * Top projects sorted by score. Public.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<ProjectResponse>> getLeaderboard() {
        return ResponseEntity.ok(projectService.getLeaderboard());
    }
}
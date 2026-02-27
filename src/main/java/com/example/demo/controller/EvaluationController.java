package com.example.demo.controller;

import com.example.demo.dto.EvaluationRequest;
import com.example.demo.dto.ProjectResponse;
import com.example.demo.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Evaluation Controller — for judges to evaluate projects.
 * Fixed: was using in-memory POJO objects, no database persistence, no
 * authorization.
 */
@RestController
@RequestMapping("/api/evaluation")
@PreAuthorize("hasAnyRole('JUDGE','ADMIN')")
public class EvaluationController {

    @Autowired
    private ProjectService projectService;

    /**
     * POST /api/evaluation/{hackathonId}/evaluate
     * Evaluate a project. Judge only.
     */
    @PostMapping("/{hackathonId}/evaluate")
    public ResponseEntity<ProjectResponse> evaluateProject(
            @PathVariable Long hackathonId,
            @Valid @RequestBody EvaluationRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        ProjectResponse response = projectService.evaluateProject(hackathonId, request, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/evaluation/{hackathonId}/projects
     * Get all projects for a hackathon (for judge review).
     */
    @GetMapping("/{hackathonId}/projects")
    public ResponseEntity<List<ProjectResponse>> getProjectsToEvaluate(
            @PathVariable Long hackathonId) {
        return ResponseEntity.ok(projectService.getAllProjects(hackathonId));
    }

    /**
     * GET /api/evaluation/leaderboard
     * Get leaderboard after evaluations.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<ProjectResponse>> getLeaderboard() {
        return ResponseEntity.ok(projectService.getLeaderboard());
    }
}
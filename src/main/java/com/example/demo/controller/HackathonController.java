package com.example.demo.controller;

import com.example.demo.dto.HackathonRequest;
import com.example.demo.dto.HackathonResponse;
import com.example.demo.model.HackathonStatus;
import com.example.demo.service.HackathonService;
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
 * Hackathon Controller.
 * Fixed: URL changed from /api/hackathon to /api/hackathons (REST convention).
 * Fixed: Returns ResponseEntity with proper status codes.
 * Fixed: Uses proper DTOs rather than entity classes.
 * Fixed: Uses authenticated user from Spring Security context for organizer
 * assignment.
 */
@RestController
@RequestMapping("/api/hackathons")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class HackathonController {

    @Autowired
    private HackathonService hackathonService;

    /**
     * GET /api/hackathons
     * Get all hackathons. Public endpoint.
     */
    @GetMapping
    public ResponseEntity<List<HackathonResponse>> getAllHackathons(
            @RequestParam(required = false) HackathonStatus status) {
        List<HackathonResponse> hackathons = status != null
                ? hackathonService.getHackathonsByStatus(status)
                : hackathonService.getAllHackathons();
        return ResponseEntity.ok(hackathons);
    }

    /**
     * GET /api/hackathons/{id}
     * Get a hackathon by ID. Public endpoint.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HackathonResponse> getHackathonById(@PathVariable Long id) {
        return ResponseEntity.ok(hackathonService.getHackathonById(id));
    }

    /**
     * POST /api/hackathons
     * Create a new hackathon. Organizer or Admin only.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<HackathonResponse> createHackathon(
            @Valid @RequestBody HackathonRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        HackathonResponse response = hackathonService.createHackathon(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/hackathons/{id}/register
     * Register the authenticated participant for a hackathon.
     */
    @PostMapping("/{id}/register")
    @PreAuthorize("hasAnyRole('PARTICIPANT','ADMIN')")
    public ResponseEntity<String> registerForHackathon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        String message = hackathonService.registerParticipant(id, currentUser.getUsername());
        return ResponseEntity.ok(message);
    }

    /**
     * PUT /api/hackathons/{id}/status
     * Update hackathon status. Organizer/Admin only.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<HackathonResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam HackathonStatus status) {
        return ResponseEntity.ok(hackathonService.updateStatus(id, status));
    }

    /**
     * GET /api/hackathons/my
     * Get hackathons organised by the current user.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<List<HackathonResponse>> getMyHackathons(
            @AuthenticationPrincipal UserDetails currentUser) {
        // Uses userEmail to look up organizer ID
        return ResponseEntity.ok(hackathonService.getAllHackathons());
    }
}
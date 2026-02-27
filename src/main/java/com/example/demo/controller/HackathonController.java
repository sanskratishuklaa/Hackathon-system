package com.example.demo.controller;

import com.example.demo.dto.HackathonRequest;
import com.example.demo.dto.HackathonResponse;
import com.example.demo.model.HackathonStatus;
import com.example.demo.service.HackathonService;
import com.example.demo.service.UserService;
import com.example.demo.model.User;
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
 * Hackathon REST Controller.
 *
 * Fixes applied:
 * - (C4) GET /my now correctly returns only the caller's own hackathons.
 * - (C5) PUT /status now passes callerEmail so service can verify ownership.
 * - (H7) DELETE endpoint added with ownership validation in service layer.
 * - (L2) Removed @CrossOrigin — global CORS is configured in SecurityConfig.
 */
@RestController
@RequestMapping("/api/hackathons")
public class HackathonController {

    @Autowired
    private HackathonService hackathonService;

    @Autowired
    private UserService userService;

    // -------------------------------------------------------------------------
    // Public endpoints
    // -------------------------------------------------------------------------

    /**
     * GET /api/hackathons
     * Get all hackathons, optionally filtered by status. Public.
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
     * Get a hackathon by ID. Public.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HackathonResponse> getHackathonById(@PathVariable Long id) {
        return ResponseEntity.ok(hackathonService.getHackathonById(id));
    }

    // -------------------------------------------------------------------------
    // Organizer / Admin endpoints
    // -------------------------------------------------------------------------

    /**
     * GET /api/hackathons/my
     * Get hackathons created by the currently authenticated organizer.
     *
     * FIX (C4): Was calling getAllHackathons() — returned every hackathon on
     * the platform. Now correctly filters by the caller's organizer ID.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<List<HackathonResponse>> getMyHackathons(
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userService.getUserByEmail(currentUser.getUsername());
        return ResponseEntity.ok(hackathonService.getHackathonsByOrganizer(user.getId()));
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
     * PUT /api/hackathons/{id}
     * Update a hackathon's details. Only the owner or Admin can do this.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<HackathonResponse> updateHackathon(
            @PathVariable Long id,
            @Valid @RequestBody HackathonRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        HackathonResponse response = hackathonService.updateHackathon(id, request, currentUser.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/hackathons/{id}/status
     * Update hackathon status. Only the owner or Admin.
     * FIX (C5): Now passes callerEmail to the service for ownership validation.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<HackathonResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam HackathonStatus status,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(hackathonService.updateStatus(id, status, currentUser.getUsername()));
    }

    /**
     * DELETE /api/hackathons/{id}
     * Delete a hackathon. Only the owner or Admin.
     * FIX (H7): Ownership validated in the service layer.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    public ResponseEntity<Void> deleteHackathon(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        hackathonService.deleteHackathon(id, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Participant endpoints
    // -------------------------------------------------------------------------

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
}
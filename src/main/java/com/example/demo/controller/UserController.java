package com.example.demo.controller;

import com.example.demo.dto.UserResponse;
import com.example.demo.model.Role;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User REST Controller — profile and admin operations.
 *
 * Fixes applied:
 * - (H2) Returns UserResponse DTO instead of the raw User entity,
 * preventing accidental exposure of internal entity fields.
 * - (L2) Removed per-controller @CrossOrigin — global CORS in SecurityConfig.
 * - Added /admin/users/{id}/role and /admin/users/{id}/active endpoints
 * so role elevation goes through a controlled, audited, ADMIN-only path.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/users/me
     * Authenticated user's own profile.
     * FIX (H2+M6): Returns UserResponse DTO, not the raw entity.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(
                userService.toUserResponse(userService.getUserByEmail(currentUser.getUsername())));
    }

    /**
     * GET /api/users/all
     * All users — Admin only.
     * FIX (H2): Returns List<UserResponse>, not List<User>.
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/users/{id}
     * Get user by ID — Admin only.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toUserResponse(userService.getUserById(id)));
    }

    /**
     * PATCH /api/users/{id}/role?role=ORGANIZER
     * Change a user's role — Admin only.
     * This is the ONLY legitimate way to elevate a user's privileges.
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        return ResponseEntity.ok(userService.changeUserRole(id, role));
    }

    /**
     * PATCH /api/users/{id}/active?active=false
     * Activate or deactivate (soft-ban) a user — Admin only.
     */
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> setUserActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(userService.setUserActive(id, active));
    }
}
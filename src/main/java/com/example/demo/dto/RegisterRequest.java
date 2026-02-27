package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for user registration requests.
 *
 * FIX (M4 — Critical privilege escalation): The original DTO had a `role`
 * field that allowed clients to self-assign ADMIN or ORGANIZER by sending
 * {"role":"ADMIN"} in the JSON body. Removed entirely.
 * New users are always registered as PARTICIPANT; role elevation is an
 * admin-only action performed via a separate API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 150, message = "College name must not exceed 150 characters")
    private String college;

    // NOTE: 'role' field intentionally omitted.
    // All registrations default to Role.PARTICIPANT in UserService.
}

package com.example.demo.dto;

import com.example.demo.model.Role;
import lombok.*;

/**
 * Safe read-only DTO for exposing User data over the API.
 *
 * FIX (H2): Replaces direct User entity serialisation in controllers.
 * The entity has @JsonIgnore on password, but still exposes internal
 * implementation details (bidirectional collections, active flag, etc.).
 * This DTO is an explicit, stable contract.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String college;
    private Role role;
    private boolean active;
    private String createdAt;

    // No password, no registrations list, no projects list.
}

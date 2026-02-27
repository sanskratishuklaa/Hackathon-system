package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service — handles registration, login, and user management.
 *
 * Fixes applied:
 * - (M4) Role is NEVER taken from the request — always defaults to PARTICIPANT.
 * - (M8) login() now uses @Transactional(readOnly=true) instead of implicit
 * write tx.
 * - (H2) getAllUsers() now returns List<UserResponse> (not raw entities).
 * - (L3) Removed @Deprecated addUser() dead code.
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    /**
     * Register a new user with role PARTICIPANT (always).
     * - Checks for duplicate email
     * - Encodes password with BCrypt
     * - Returns JWT token + user info on success
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        // FIX (M4): role is ALWAYS PARTICIPANT on self-registration.
        // Privilege elevation must go through a separate admin-only endpoint.
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .college(request.getCollege())
                .role(Role.PARTICIPANT)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("New user registered: {} [{}]", savedUser.getEmail(), savedUser.getRole());

        String token = jwtService.generateToken(savedUser.getEmail());
        return AuthResponse.of(token, savedUser.getId(), savedUser.getName(),
                savedUser.getEmail(), savedUser.getRole());
    }

    /**
     * Authenticate a user and return JWT token.
     * FIX (M8): Uses readOnly=true — login only reads, no writes needed.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user.getEmail());
        logger.info("User logged in: {}", user.getEmail());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    // -------------------------------------------------------------------------
    // User lookups
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * FIX (H2): Returns safe UserResponse DTOs instead of raw User entities.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    /**
     * Admin-only: change a user's role.
     * This is the ONLY way to promote a user beyond PARTICIPANT.
     */
    public UserResponse changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setRole(newRole);
        User saved = userRepository.save(user);
        logger.info("User {} role changed to {}", saved.getEmail(), newRole);
        return toUserResponse(saved);
    }

    /**
     * Admin-only: toggle user active status (soft ban).
     */
    public UserResponse setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setActive(active);
        User saved = userRepository.save(user);
        logger.info("User {} active status set to {}", saved.getEmail(), active);
        return toUserResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    public UserResponse toUserResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .college(u.getCollege())
                .role(u.getRole())
                .active(u.isActive())
                .createdAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null)
                .build();
    }
}
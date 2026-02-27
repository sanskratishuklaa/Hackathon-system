package com.example.demo.service;

import com.example.demo.dto.HackathonRequest;
import com.example.demo.dto.HackathonResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Hackathon;
import com.example.demo.model.HackathonStatus;
import com.example.demo.model.Registration;
import com.example.demo.model.RegistrationStatus;
import com.example.demo.model.User;
import com.example.demo.repository.HackathonRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Hackathon Service — handles CRUD and registration logic.
 * Fixed: was using in-memory list; now uses MySQL via JPA repositories.
 */
@Service
@Transactional
public class HackathonService {

    private static final Logger logger = LoggerFactory.getLogger(HackathonService.class);

    @Autowired
    private HackathonRepository hackathonRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Create a new hackathon (Organizer or Admin only).
     */
    public HackathonResponse createHackathon(HackathonRequest request, String organizerEmail) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Hackathon hackathon = Hackathon.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxParticipants(request.getMaxParticipants())
                .prizeAmount(request.getPrizeAmount())
                .status(HackathonStatus.UPCOMING)
                .organizer(organizer)
                .build();

        Hackathon saved = hackathonRepository.save(hackathon);
        logger.info("Hackathon created: {} by {}", saved.getName(), organizerEmail);
        return toResponse(saved);
    }

    /**
     * Get all hackathons (public endpoint).
     */
    @Transactional(readOnly = true)
    public List<HackathonResponse> getAllHackathons() {
        return hackathonRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get hackathons by status.
     */
    @Transactional(readOnly = true)
    public List<HackathonResponse> getHackathonsByStatus(HackathonStatus status) {
        return hackathonRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific hackathon by ID.
     */
    @Transactional(readOnly = true)
    public HackathonResponse getHackathonById(Long id) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon not found with id: " + id));
        return toResponse(hackathon);
    }

    /**
     * Get hackathons organised by a specific user.
     */
    @Transactional(readOnly = true)
    public List<HackathonResponse> getHackathonsByOrganizer(Long organizerId) {
        return hackathonRepository.findByOrganizerId(organizerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Register a participant for a hackathon.
     * Validates: hackathon exists, not full, user not already registered.
     */
    public String registerParticipant(Long hackathonId, String userEmail) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon not found with id: " + hackathonId));

        if (hackathon.getStatus() == HackathonStatus.COMPLETED ||
                hackathon.getStatus() == HackathonStatus.CANCELLED) {
            throw new BadRequestException("Cannot register for a " + hackathon.getStatus() + " hackathon");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (registrationRepository.existsByUserIdAndHackathonId(user.getId(), hackathonId)) {
            throw new BadRequestException("You are already registered for this hackathon");
        }

        long currentCount = registrationRepository.countActiveByHackathonId(hackathonId);
        if (currentCount >= hackathon.getMaxParticipants()) {
            throw new BadRequestException("Hackathon is full. Maximum participants: " + hackathon.getMaxParticipants());
        }

        Registration registration = Registration.builder()
                .user(user)
                .hackathon(hackathon)
                .status(RegistrationStatus.REGISTERED)
                .build();

        registrationRepository.save(registration);
        logger.info("User {} registered for hackathon {}", userEmail, hackathon.getName());
        return "Successfully registered for " + hackathon.getName();
    }

    /**
     * Update hackathon status.
     */
    public HackathonResponse updateStatus(Long id, HackathonStatus status) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon not found with id: " + id));
        hackathon.setStatus(status);
        return toResponse(hackathonRepository.save(hackathon));
    }

    /**
     * Count all hackathons by status (for stats).
     */
    @Transactional(readOnly = true)
    public long countByStatus(HackathonStatus status) {
        return hackathonRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return hackathonRepository.count();
    }

    /**
     * Map Hackathon entity to HackathonResponse DTO.
     */
    private HackathonResponse toResponse(Hackathon h) {
        long regCount = registrationRepository.countActiveByHackathonId(h.getId());
        long projCount = projectRepository.countByHackathonId(h.getId());

        return HackathonResponse.builder()
                .id(h.getId())
                .name(h.getName())
                .description(h.getDescription())
                .location(h.getLocation())
                .startDate(h.getStartDate())
                .endDate(h.getEndDate())
                .maxParticipants(h.getMaxParticipants())
                .prizeAmount(h.getPrizeAmount())
                .status(h.getStatus())
                .organizerName(h.getOrganizer() != null ? h.getOrganizer().getName() : null)
                .organizerId(h.getOrganizer() != null ? h.getOrganizer().getId() : null)
                .registrationCount(regCount)
                .projectCount(projCount)
                .createdAt(h.getCreatedAt())
                .build();
    }
}
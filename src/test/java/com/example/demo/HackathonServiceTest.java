package com.example.demo;

import com.example.demo.exception.BadRequestException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.HackathonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for HackathonService business logic.
 * Tests the key fixes: ownership checks, status validation, registration cap.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional // rolls back after each test
class HackathonServiceTest {

    @Autowired
    private HackathonService hackathonService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HackathonRepository hackathonRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    private User organizer;
    private User otherOrganizer;
    private User participant;

    @BeforeEach
    void setUp() {
        organizer = userRepository.save(User.builder()
                .name("Organizer One")
                .email("org1@test.com")
                .password("$2a$12$dummyHash111111111111111111111111111111111111111111111")
                .role(Role.ORGANIZER)
                .active(true)
                .build());

        otherOrganizer = userRepository.save(User.builder()
                .name("Organizer Two")
                .email("org2@test.com")
                .password("$2a$12$dummyHash222222222222222222222222222222222222222222222")
                .role(Role.ORGANIZER)
                .active(true)
                .build());

        participant = userRepository.save(User.builder()
                .name("Participant One")
                .email("part1@test.com")
                .password("$2a$12$dummyHash333333333333333333333333333333333333333333333")
                .role(Role.PARTICIPANT)
                .active(true)
                .build());
    }

    // ── Date validation ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createHackathon: end date before start date → BadRequestException")
    void createHackathon_endBeforeStart_throws() {
        com.example.demo.dto.HackathonRequest req = com.example.demo.dto.HackathonRequest.builder()
                .name("Bad Dates Hack")
                .location("Online")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(5)) // before start!
                .maxParticipants(50)
                .prizeAmount(1000.0)
                .build();

        assertThatThrownBy(() -> hackathonService.createHackathon(req, organizer.getEmail()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    @DisplayName("createHackathon: valid request → hackathon created with UPCOMING status")
    void createHackathon_valid_created() {
        com.example.demo.dto.HackathonRequest req = com.example.demo.dto.HackathonRequest.builder()
                .name("Spring Hack 2026")
                .location("Online")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .maxParticipants(50)
                .prizeAmount(5000.0)
                .build();

        var response = hackathonService.createHackathon(req, organizer.getEmail());

        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(HackathonStatus.UPCOMING);
        assertThat(response.getOrganizerName()).isEqualTo("Organizer One");
    }

    // ── Ownership checks (FIX C5 & H7) ──────────────────────────────────────

    @Test
    @DisplayName("updateStatus: non-owner organizer → BadRequestException  (FIX C5)")
    void updateStatus_byNonOwner_throws() {
        // Create hackathon owned by org1
        Hackathon h = hackathonRepository.save(Hackathon.builder()
                .name("Owned Hack")
                .location("Pune")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .maxParticipants(100)
                .status(HackathonStatus.UPCOMING)
                .organizer(organizer)
                .build());

        // org2 tries to update status
        assertThatThrownBy(
                () -> hackathonService.updateStatus(h.getId(), HackathonStatus.ACTIVE, otherOrganizer.getEmail()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("updateStatus: owner → status updated successfully")
    void updateStatus_byOwner_succeeds() {
        Hackathon h = hackathonRepository.save(Hackathon.builder()
                .name("My Hack")
                .location("Delhi")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .maxParticipants(100)
                .status(HackathonStatus.UPCOMING)
                .organizer(organizer)
                .build());

        var response = hackathonService.updateStatus(h.getId(), HackathonStatus.ACTIVE, organizer.getEmail());
        assertThat(response.getStatus()).isEqualTo(HackathonStatus.ACTIVE);
    }

    // ── Registration ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerParticipant: successful registration")
    void registerParticipant_success() {
        Hackathon h = hackathonRepository.save(Hackathon.builder()
                .name("Open Hack")
                .location("Mumbai")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .maxParticipants(10)
                .status(HackathonStatus.UPCOMING)
                .organizer(organizer)
                .build());

        String result = hackathonService.registerParticipant(h.getId(), participant.getEmail());
        assertThat(result).contains("Successfully registered");
    }

    @Test
    @DisplayName("registerParticipant: duplicate registration → BadRequestException")
    void registerParticipant_duplicate_throws() {
        Hackathon h = hackathonRepository.save(Hackathon.builder()
                .name("Dup Reg Hack")
                .location("Hyderabad")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .maxParticipants(10)
                .status(HackathonStatus.UPCOMING)
                .organizer(organizer)
                .build());

        hackathonService.registerParticipant(h.getId(), participant.getEmail());

        assertThatThrownBy(() -> hackathonService.registerParticipant(h.getId(), participant.getEmail()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("registerParticipant: hackathon full → BadRequestException")
    void registerParticipant_hackathonFull_throws() {
        // maxParticipants = 1
        Hackathon h = hackathonRepository.save(Hackathon.builder()
                .name("Full Hack")
                .location("Bangalore")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .maxParticipants(1)
                .status(HackathonStatus.UPCOMING)
                .organizer(organizer)
                .build());

        hackathonService.registerParticipant(h.getId(), participant.getEmail());

        // Second participant
        User part2 = userRepository.save(User.builder()
                .name("Part Two")
                .email("part2@test.com")
                .password("$2a$12$dummyHash444444444444444444444444444444444444444444444")
                .role(Role.PARTICIPANT)
                .active(true)
                .build());

        assertThatThrownBy(() -> hackathonService.registerParticipant(h.getId(), part2.getEmail()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("full");
    }

    @Test
    @DisplayName("registerParticipant: CANCELLED hackathon → BadRequestException")
    void registerParticipant_cancelledHackathon_throws() {
        Hackathon h = hackathonRepository.save(Hackathon.builder()
                .name("Cancelled Hack")
                .location("Chennai")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(5))
                .maxParticipants(100)
                .status(HackathonStatus.CANCELLED)
                .organizer(organizer)
                .build());

        assertThatThrownBy(() -> hackathonService.registerParticipant(h.getId(), participant.getEmail()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CANCELLED");
    }
}

package com.example.demo.repository;

import com.example.demo.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Find a specific registration by user and hackathon
    Optional<Registration> findByUserIdAndHackathonId(Long userId, Long hackathonId);

    // Check if user already registered for a hackathon
    boolean existsByUserIdAndHackathonId(Long userId, Long hackathonId);

    // Get all registrations for a specific hackathon
    List<Registration> findByHackathonId(Long hackathonId);

    // Get all registrations for a specific user
    List<Registration> findByUserId(Long userId);

    // Count participants in a hackathon
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.hackathon.id = :hackathonId AND r.status != 'CANCELLED'")
    long countActiveByHackathonId(@Param("hackathonId") Long hackathonId);

    // Total registrations across all hackathons
    @Query("SELECT COUNT(r) FROM Registration r")
    long countTotalRegistrations();
}
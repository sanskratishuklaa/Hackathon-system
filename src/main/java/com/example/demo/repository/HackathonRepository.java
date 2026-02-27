package com.example.demo.repository;

import com.example.demo.model.Hackathon;
import com.example.demo.model.HackathonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HackathonRepository extends JpaRepository<Hackathon, Long> {

    List<Hackathon> findByStatus(HackathonStatus status);

    List<Hackathon> findByOrganizerId(Long organizerId);

    List<Hackathon> findByStatusOrderByStartDateAsc(HackathonStatus status);

    @Query("SELECT h FROM Hackathon h WHERE h.name LIKE %:keyword% OR h.description LIKE %:keyword%")
    List<Hackathon> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(h) FROM Hackathon h WHERE h.status = :status")
    long countByStatus(@Param("status") HackathonStatus status);
}
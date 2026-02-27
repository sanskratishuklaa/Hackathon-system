package com.example.demo.repository;

import com.example.demo.model.Project;
import com.example.demo.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByHackathonId(Long hackathonId);

    List<Project> findBySubmittedById(Long userId);

    List<Project> findByStatusOrderByScoreDesc(ProjectStatus status);

    List<Project> findByHackathonIdAndStatus(Long hackathonId, ProjectStatus status);

    boolean existsByHackathonIdAndSubmittedById(Long hackathonId, Long userId);

    @Query("SELECT p FROM Project p ORDER BY p.score DESC")
    List<Project> findLeaderboard();

    @Query("SELECT COUNT(p) FROM Project p WHERE p.hackathon.id = :hackathonId")
    long countByHackathonId(@Param("hackathonId") Long hackathonId);

    @Query("SELECT COUNT(p) FROM Project p")
    long countAllProjects();
}
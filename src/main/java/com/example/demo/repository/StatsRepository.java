package com.example.demo.repository;

import com.example.demo.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p.title AS title, p.score AS score, p.hackathon.name AS hackathonName " +
            "FROM Project p ORDER BY p.score DESC")
    List<StatsProjection> getTopProjects();
}
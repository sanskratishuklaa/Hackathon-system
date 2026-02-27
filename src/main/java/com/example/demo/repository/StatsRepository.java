package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.demo.model.Project;
import java.util.List;

public interface StatsRepository extends JpaRepository<Project, Integer> {

    @Query("SELECT p.name as name, p.dnaScore as dnaScore FROM Project p ORDER BY p.dnaScore DESC")
    List<StatsProjection> getTopProjects();
}
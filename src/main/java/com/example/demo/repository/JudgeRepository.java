package com.example.demo.repository;

import com.example.demo.model.Judge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JudgeRepository extends JpaRepository<Judge, Long> {

    List<Judge> findByHackathonId(Long hackathonId);

    Optional<Judge> findByUserIdAndHackathonId(Long userId, Long hackathonId);

    boolean existsByUserIdAndHackathonId(Long userId, Long hackathonId);
}

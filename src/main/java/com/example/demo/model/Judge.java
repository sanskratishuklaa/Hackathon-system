package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "judges", indexes = {
        @Index(name = "idx_judge_hackathon", columnList = "hackathon_id"),
        @Index(name = "idx_judge_user", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Judge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id", nullable = false)
    @JsonIgnore
    private Hackathon hackathon;

    @Size(max = 200)
    @Column(length = 200)
    private String expertise;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "projects_evaluated")
    @Builder.Default
    private Integer projectsEvaluated = 0;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    // Helper to check if judge can still evaluate (max 20 projects)
    @Transient
    public boolean canEvaluate() {
        return projectsEvaluated < 20;
    }

    public void incrementEvaluations() {
        this.projectsEvaluated++;
    }
}
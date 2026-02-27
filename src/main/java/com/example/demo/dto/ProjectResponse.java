package com.example.demo.dto;

import com.example.demo.model.ProjectStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String techStack;
    private String githubUrl;
    private String demoUrl;
    private Integer score;
    private ProjectStatus status;
    private String judgeFeedback;
    private String evaluatedBy;
    private String hackathonName;
    private Long hackathonId;
    private String submittedByName;
    private Long submittedById;
    private LocalDateTime submittedAt;
    private LocalDateTime evaluatedAt;
}

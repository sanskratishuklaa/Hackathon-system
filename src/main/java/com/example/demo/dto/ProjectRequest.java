package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequest {

    @NotBlank(message = "Project title is required")
    @Size(min = 3, max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @Size(max = 500)
    private String techStack;

    @Size(max = 500)
    private String githubUrl;

    @Size(max = 500)
    private String demoUrl;

    @NotNull(message = "Hackathon ID is required")
    private Long hackathonId;
}

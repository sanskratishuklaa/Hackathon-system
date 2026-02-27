package com.example.demo.service;

import com.example.demo.dto.EvaluationRequest;
import com.example.demo.dto.ProjectRequest;
import com.example.demo.dto.ProjectResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project (Submission & Evaluation) Service.
 * Fixed: was using in-memory lists and non-JPA POJO objects.
 * Now persists submissions/evaluations to MySQL.
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private HackathonRepository hackathonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private JudgeRepository judgeRepository;

    /**
     * Submit a project for a hackathon.
     * Validates: hackathon exists, user is registered, no duplicate submission.
     */
    public ProjectResponse submitProject(ProjectRequest request, String userEmail) {
        Hackathon hackathon = hackathonRepository.findById(request.getHackathonId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hackathon not found with id: " + request.getHackathonId()));

        if (hackathon.getStatus() == HackathonStatus.COMPLETED ||
                hackathon.getStatus() == HackathonStatus.CANCELLED) {
            throw new BadRequestException("Cannot submit to a " + hackathon.getStatus() + " hackathon");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify user is registered for this hackathon
        if (!registrationRepository.existsByUserIdAndHackathonId(user.getId(), hackathon.getId())) {
            throw new BadRequestException("You must register for the hackathon before submitting a project");
        }

        // Prevent duplicate submissions
        if (projectRepository.existsByHackathonIdAndSubmittedById(hackathon.getId(), user.getId())) {
            throw new BadRequestException("You have already submitted a project for this hackathon");
        }

        Project project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(request.getTechStack())
                .githubUrl(request.getGithubUrl())
                .demoUrl(request.getDemoUrl())
                .hackathon(hackathon)
                .submittedBy(user)
                .status(ProjectStatus.SUBMITTED)
                .score(0)
                .build();

        Project saved = projectRepository.save(project);
        logger.info("Project '{}' submitted by {} for hackathon '{}'",
                saved.getTitle(), userEmail, hackathon.getName());
        return toResponse(saved);
    }

    /**
     * Evaluate a project (Judge only).
     */
    public ProjectResponse evaluateProject(Long hackathonId, EvaluationRequest request, String judgeEmail) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        // Verify evaluator is a judge for this hackathon
        User judge = userRepository.findByEmail(judgeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Judge not found"));

        // Check judge is assigned to this hackathon
        if (hackathonId != null) {
            if (!judgeRepository.existsByUserIdAndHackathonId(judge.getId(), hackathonId)) {
                throw new BadRequestException("You are not assigned as a judge for this hackathon");
            }
        }

        // Determine status based on score
        ProjectStatus status;
        if (request.getScore() >= 80) {
            status = ProjectStatus.WINNER;
        } else if (request.getScore() >= 60) {
            status = ProjectStatus.ACCEPTED;
        } else {
            status = ProjectStatus.REJECTED;
        }

        project.setScore(request.getScore());
        project.setJudgeFeedback(request.getFeedback());
        project.setStatus(status);
        project.setEvaluatedBy(judgeEmail);
        project.setEvaluatedAt(LocalDateTime.now());

        // Increment judge evaluation count
        judgeRepository.findByUserIdAndHackathonId(judge.getId(), project.getHackathon().getId())
                .ifPresent(j -> {
                    j.incrementEvaluations();
                    judgeRepository.save(j);
                });

        Project saved = projectRepository.save(project);
        logger.info("Project '{}' evaluated by {} — Score: {}, Status: {}",
                saved.getTitle(), judgeEmail, request.getScore(), status);
        return toResponse(saved);
    }

    /**
     * Get all projects (filtered by hackathon if provided).
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(Long hackathonId) {
        List<Project> projects = hackathonId != null
                ? projectRepository.findByHackathonId(hackathonId)
                : projectRepository.findAll();
        return projects.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get projects by the current user.
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return projectRepository.findBySubmittedById(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Leaderboard — top projects sorted by score descending.
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getLeaderboard() {
        return projectRepository.findLeaderboard()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countAllProjects() {
        return projectRepository.countAllProjects();
    }

    /**
     * Map Project entity to ProjectResponse DTO.
     */
    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .techStack(p.getTechStack())
                .githubUrl(p.getGithubUrl())
                .demoUrl(p.getDemoUrl())
                .score(p.getScore())
                .status(p.getStatus())
                .judgeFeedback(p.getJudgeFeedback())
                .evaluatedBy(p.getEvaluatedBy())
                .hackathonName(p.getHackathon() != null ? p.getHackathon().getName() : null)
                .hackathonId(p.getHackathon() != null ? p.getHackathon().getId() : null)
                .submittedByName(p.getSubmittedBy() != null ? p.getSubmittedBy().getName() : null)
                .submittedById(p.getSubmittedBy() != null ? p.getSubmittedBy().getId() : null)
                .submittedAt(p.getSubmittedAt())
                .evaluatedAt(p.getEvaluatedAt())
                .build();
    }
}

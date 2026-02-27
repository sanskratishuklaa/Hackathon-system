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
 * Project Submission & Evaluation Service.
 *
 * Fixes applied:
 * - (H3) Score thresholds extracted as named constants — no more magic numbers.
 * - Evaluation validates judge assignment is not null before checking.
 * - toResponse() handles null hackathon/submitter safely.
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    // FIX (H3): Named constants instead of magic numbers in evaluateProject().
    // These can be moved to application.properties + @Value if per-hackathon
    // thresholds are needed in the future.
    private static final int WINNER_SCORE_THRESHOLD = 80;
    private static final int ACCEPTED_SCORE_THRESHOLD = 60;

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

    // -------------------------------------------------------------------------
    // Submission
    // -------------------------------------------------------------------------

    /**
     * Submit a project for a hackathon.
     * Validates: hackathon exists & is open, user is registered,
     * user hasn't already submitted a project.
     */
    public ProjectResponse submitProject(ProjectRequest request, String userEmail) {
        Hackathon hackathon = hackathonRepository.findById(request.getHackathonId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hackathon not found with id: " + request.getHackathonId()));

        if (hackathon.getStatus() == HackathonStatus.COMPLETED ||
                hackathon.getStatus() == HackathonStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cannot submit to a " + hackathon.getStatus() + " hackathon");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!registrationRepository.existsByUserIdAndHackathonId(user.getId(), hackathon.getId())) {
            throw new BadRequestException(
                    "You must register for the hackathon before submitting a project");
        }

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

    // -------------------------------------------------------------------------
    // Evaluation
    // -------------------------------------------------------------------------

    /**
     * Evaluate a project (Judge or Admin only).
     * FIX (H3): Score thresholds are now named constants.
     */
    public ProjectResponse evaluateProject(Long hackathonId, EvaluationRequest request, String judgeEmail) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id: " + request.getProjectId()));

        User judge = userRepository.findByEmail(judgeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Judge not found"));

        // Verify judge is assigned to this hackathon (only if hackathonId provided)
        if (hackathonId != null) {
            if (!judgeRepository.existsByUserIdAndHackathonId(judge.getId(), hackathonId)) {
                throw new BadRequestException("You are not assigned as a judge for this hackathon");
            }
        }

        // FIX (H3): Use named constants instead of bare magic numbers
        ProjectStatus status;
        if (request.getScore() >= WINNER_SCORE_THRESHOLD) {
            status = ProjectStatus.WINNER;
        } else if (request.getScore() >= ACCEPTED_SCORE_THRESHOLD) {
            status = ProjectStatus.ACCEPTED;
        } else {
            status = ProjectStatus.REJECTED;
        }

        project.setScore(request.getScore());
        project.setJudgeFeedback(request.getFeedback());
        project.setStatus(status);
        project.setEvaluatedBy(judgeEmail);
        project.setEvaluatedAt(LocalDateTime.now());

        // Increment judge's evaluation count
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

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(Long hackathonId) {
        List<Project> projects = hackathonId != null
                ? projectRepository.findByHackathonId(hackathonId)
                : projectRepository.findAll();
        return projects.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return projectRepository.findBySubmittedById(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getLeaderboard() {
        return projectRepository.findLeaderboard()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countAllProjects() {
        return projectRepository.countAllProjects();
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

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

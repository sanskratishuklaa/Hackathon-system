package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.EvaluationRequest;
import com.example.demo.model.Project;
import com.example.demo.model.Judge;
import com.example.demo.service.EvaluationService;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    // Single or multiple judges evaluation
    @PostMapping("/evaluate")
    public String evaluateProject(@RequestBody EvaluationRequest request) {
        Project project = request.getProject();
        List<Judge> judges = request.getJudges();

        if (judges.size() == 1) {
            return evaluationService.evaluateProject(project, judges.get(0));
        } else {
            return evaluationService.evaluateProjectByJudges(project, judges);
        }
    }

    // Get all evaluated projects
    @GetMapping("/all")
    public List<Project> getAllEvaluatedProjects() {
        return evaluationService.getEvaluatedProjects();
    }

    // Get evaluation history
    @GetMapping("/history")
    public List<String> getEvaluationHistory() {
        return evaluationService.getEvaluationHistory();
    }
}
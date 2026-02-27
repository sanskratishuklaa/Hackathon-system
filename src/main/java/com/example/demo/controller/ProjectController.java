package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.EvaluationRequest;
import com.example.demo.model.Judge;
import com.example.demo.model.Project;
import com.example.demo.service.EvaluationService;

@RestController
@RequestMapping("/api/projects") // base URL
@CrossOrigin 
public class ProjectController {

    @Autowired
    private EvaluationService evaluationService; // service layer inject

    
    @PostMapping("/evaluate")
    public String evaluateProject(@RequestBody EvaluationRequest request) {
        Project project = request.getProject();
        List<Judge> judges = request.getJudges();

       
        if (project == null || judges == null || judges.isEmpty()) {
            return "Project or Judges data missing!";
        }

        
        if (judges.size() == 1) {
            return evaluationService.evaluateProject(project, judges.get(0));
        } 
        // Multiple judges
        else {
            return evaluationService.evaluateProjectByJudges(project, judges);
        }
    }

    
    @GetMapping("/evaluated")
    public List<Project> getEvaluatedProjects() {
        return evaluationService.getEvaluatedProjects();
    }

    
    @GetMapping("/history")
    public List<String> getEvaluationHistory() {
        return evaluationService.getEvaluationHistory();
    }
}
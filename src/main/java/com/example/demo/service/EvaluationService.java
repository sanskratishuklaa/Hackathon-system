package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.example.demo.model.Judge;
import com.example.demo.model.Project;

@Service
public class EvaluationService {
    private List<Project> evaluatedProjects = new ArrayList<>();
    private List<String> evaluationHistory = new ArrayList<>();

    public String evaluateProject(Project project, Judge judge) {
        if(!judge.canEvaluate()) return "Judge fatigue alert ⚠️";

        int dnaScore = project.calculateDNAScore();
        judge.increaseFatigue();
        evaluatedProjects.add(project);

        List<String> feedback = new ArrayList<>();
        if(dnaScore >= 16) { project.setStatus("Winner"); feedback.add("Excellent DNA score!"); }
        else if(dnaScore >= 12) { project.setStatus("Accepted"); feedback.add("Good project. Minor improvements."); }
        else { project.setStatus("Rejected"); feedback.add("Low DNA score."); }

        project.setReviews(feedback);
        project.setEvaluatedBy(judge.getName());

        evaluationHistory.add("Project " + project.getName() + " evaluated by " + judge.getName() + " | Score: " + dnaScore + " | Status: " + project.getStatus());
        return "Evaluated: " + project.getName() + " | Status: " + project.getStatus();
    }

    public String evaluateProjectByJudges(Project project, List<Judge> judges) {
        StringBuilder sb = new StringBuilder();
        for(Judge j: judges) sb.append(evaluateProject(project, j)).append("\n");
        return sb.toString();
    }

    public List<Project> getEvaluatedProjects() { return evaluatedProjects; }
    public List<String> getEvaluationHistory() { return evaluationHistory; }
}
package com.example.demo.dto;

import java.util.List;
import com.example.demo.model.Judge;
import com.example.demo.model.Project;

public class EvaluationRequest {
    private Project project;
    private List<Judge> judges;

    public EvaluationRequest() {}

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public List<Judge> getJudges() { return judges; }
    public void setJudges(List<Judge> judges) { this.judges = judges; }
}
package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;
    private int dnaScore;
    private String status;
    private List<String> reviews = new ArrayList<>();
    private String evaluatedBy;

    public Project() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDnaScore() { return dnaScore; }
    public void setDnaScore(int dnaScore) { this.dnaScore = dnaScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getReviews() { return reviews; }
    public void setReviews(List<String> reviews) { this.reviews = reviews; }

    public String getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(String evaluatedBy) { this.evaluatedBy = evaluatedBy; }

    public int calculateDNAScore() { return dnaScore; }
}
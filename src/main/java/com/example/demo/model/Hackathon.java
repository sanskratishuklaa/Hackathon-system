package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Hackathon {
    private int hackathonId;
    private String name;
    private List<User> participants = new ArrayList<>();
    private List<Judge> judges = new ArrayList<>();
    private List<String> results = new ArrayList<>();

    public Hackathon() {}

    public int getHackathonId() { return hackathonId; }
    public void setHackathonId(int hackathonId) { this.hackathonId = hackathonId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<User> getParticipants() { return participants; }
    public void setParticipants(List<User> participants) { this.participants = participants; }

    public List<Judge> getJudges() { return judges; }
    public void setJudges(List<Judge> judges) { this.judges = judges; }

    public List<String> getResults() { return results; }
    public void setResults(List<String> results) { this.results = results; }
}
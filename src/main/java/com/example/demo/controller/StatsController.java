package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Hackathon;
import com.example.demo.model.Judge;
import com.example.demo.model.Project;
import com.example.demo.service.EvaluationService;
import com.example.demo.service.HackathonService;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin
public class StatsController {

    @Autowired
    private HackathonService hackathonService;

    @Autowired
    private EvaluationService evaluationService;

    // ✅ Leaderboard: DNA score ke basis pe sort karke return kare
    @GetMapping("/leaderboard")
    public List<Project> getLeaderboard() {
        return evaluationService.getEvaluatedProjects().stream()
                .sorted((p1, p2) -> p2.getDnaScore() - p1.getDnaScore())
                .collect(Collectors.toList());
    }

    // ✅ Judge fatigue: POST request me judges list bhejna hoga
    @PostMapping("/judge-fatigue")
    public List<String> getJudgeFatigue(@RequestBody List<Judge> judges) {
        return judges.stream()
                .map(j -> j.getName() + " | Fatigue: " + j.getFatigue())
                .collect(Collectors.toList());
    }

    // ✅ Hackathon participants: sab hackathon ke participants ka count
    @GetMapping("/hackathon-participants")
    public List<String> getHackathonParticipants() {
        return hackathonService.getAllHackathons().stream()
                .map(h -> h.getName() + " | Participants: " + h.getParticipants().size())
                .collect(Collectors.toList());
    }
}
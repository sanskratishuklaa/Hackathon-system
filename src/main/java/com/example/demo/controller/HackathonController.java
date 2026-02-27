package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Hackathon;
import com.example.demo.model.User;
import com.example.demo.service.HackathonService;

@RestController
@RequestMapping("/api/hackathon")
@CrossOrigin
public class HackathonController {

    @Autowired
    private HackathonService hackathonService;

    @PostMapping("/register")
    public String registerHackathon(@RequestBody Hackathon hackathon){
        hackathonService.addHackathon(hackathon);
        return "Hackathon " + hackathon.getName() + " registered successfully!";
    }

    @PostMapping("/join")
    public String joinHackathon(@RequestParam int hackathonId, @RequestBody User user) {
        Hackathon h = hackathonService.getHackathonById(hackathonId);
        if(h != null){
            h.getParticipants().add(user);
            return user.getName() + " joined " + h.getName();
        }
        return "Hackathon not found";
    }

    @GetMapping("/explore")
    public List<Hackathon> exploreHackathons() {
        return hackathonService.getAllHackathons();
    }

    @GetMapping("/results")
    public List<String> getResults(@RequestParam int hackathonId){
        Hackathon h = hackathonService.getHackathonById(hackathonId);
        if(h != null) return h.getResults();
        return null;
    }
}
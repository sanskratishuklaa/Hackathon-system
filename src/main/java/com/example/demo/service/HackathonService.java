package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.example.demo.model.Hackathon;

@Service
public class HackathonService {
    private List<Hackathon> hackathons = new ArrayList<>();

    public void addHackathon(Hackathon hackathon) { hackathons.add(hackathon); }
    public List<Hackathon> getAllHackathons() { return hackathons; }

    public Hackathon getHackathonById(int id) {
        return hackathons.stream().filter(h -> h.getHackathonId() == id).findFirst().orElse(null);
    }
}
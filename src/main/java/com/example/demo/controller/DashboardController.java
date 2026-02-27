package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.Hackathon;
import com.example.demo.model.User;
import com.example.demo.service.HackathonService;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private HackathonService hackathonService;

    @Autowired
    private UserService userService;

    // Get all hackathons
    @GetMapping("/hackathons")
    public List<Hackathon> getAllHackathons() {
        return hackathonService.getAllHackathons();
    }

    // Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
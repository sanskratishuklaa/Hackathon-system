package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) { return userService.addUser(user); }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User dbUser = userService.getUserByEmail(user.getEmail());
        if(dbUser != null && dbUser.getPassword().equals(user.getPassword()))
            return "Login success! Welcome " + dbUser.getName();
        return "Invalid email or password";
    }
}
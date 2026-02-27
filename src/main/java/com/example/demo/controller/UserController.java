package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody User user) {
        for(User u : userService.getAllUsers()) {
            if(u.getEmail().equals(user.getEmail()) && u.getPassword().equals(user.getPassword())){
                return "Login Successful | Welcome " + u.getName();
            }
        }
        return "Invalid Email or Password";
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
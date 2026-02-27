package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.example.demo.model.User;

@Service
public class UserService {
    private List<User> users = new ArrayList<>();

    public User addUser(User user) { users.add(user); return user; }
    public List<User> getAllUsers() { return users; }

    public User getUserByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);
    }
}
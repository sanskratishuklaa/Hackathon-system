package com.example.demo.Security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    // In-memory user list for demo
    private List<User> users = new ArrayList<>();

    // Load user by username (email)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = users.stream()
                .filter(u -> u.getEmail().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER") // simple role
                .build();
    }

    // Custom method to get user by email
    public User getUserByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    // Custom method to save user
    public User saveUser(User user) {
        users.add(user);
        return user;
    }

    // Optional: get all users
    public List<User> getAllUsers() {
        return users;
    }
}
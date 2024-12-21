package com.example.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/add")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping("/details")
        public User getUserByEmail(@RequestParam String email) {
            return userService.getUserByEmail(email);
    }

   @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
    String email = credentials.get("email");
    String password = credentials.get("password");

    User user = userService.authenticateUser(email, password);

    if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid email or password"));
    }

    // Explicitly convert enum to string
    String roleString = user.getRole().name(); // This is already correct

    // Create a response that includes user details and role
    Map<String, Object> response = new HashMap<>();
    response.put("token", generateToken(user));
    response.put("role", roleString);
    response.put("email", user.getEmail());
    response.put("username", user.getUsername());
    response.put("id", user.getId());

    return ResponseEntity.ok(response);
}
    private String generateToken(User user) {
        // In a real-world scenario, use a proper JWT token generation
        return ("userId=" + user.getId() + ", email=" + user.getEmail() + ", role=" + user.getRole());
    }

    //forgot and reset password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            userService.sendPasswordResetEmail(email);
            return ResponseEntity.ok().body(Map.of("message", "Password reset link sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error sending reset link"));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok().body(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error resetting password"));
        }
    }
}
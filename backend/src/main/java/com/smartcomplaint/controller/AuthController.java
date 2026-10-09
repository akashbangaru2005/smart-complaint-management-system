package com.smartcomplaint.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @PostMapping("/user")
    public ResponseEntity<?> user(@RequestBody Map<String, String> body) {
        if ("1234".equals(body.getOrDefault("pin", ""))) {
            return ResponseEntity.ok(Map.of("success", true, "role", "USER"));
        }
        return ResponseEntity.status(401).body(Map.of("message", "Invalid user PIN"));
    }

    @PostMapping("/admin")
    public ResponseEntity<?> admin(@RequestBody Map<String, String> body) {
        if (adminPassword.equals(body.getOrDefault("password", ""))) {
            return ResponseEntity.ok(Map.of("success", true, "role", "ADMIN"));
        }
        return ResponseEntity.status(401).body(Map.of("message", "Invalid admin password"));
    }
}

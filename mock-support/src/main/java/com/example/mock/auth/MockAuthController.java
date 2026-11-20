package com.example.mock.auth;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Profile({"auth", "default"})
public class MockAuthController {

    public static final String DEFAULT_MOCK_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock-valid-token-admin.practice5";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody(required = false) Map<String, Object> credentials) {
        String username = "admin";
        if (credentials != null && credentials.containsKey("username")) {
            username = credentials.get("username").toString();
        }

        return ResponseEntity.ok(Map.of(
                "token", DEFAULT_MOCK_TOKEN,
                "tokenType", "Bearer",
                "expiresIn", 86400,
                "username", username,
                "role", "ADMIN",
                "message", "Authentication successful"
        ));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateGet(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return processValidation(authHeader, null);
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validatePost(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> body) {
        String bodyToken = (body != null && body.containsKey("token")) ? body.get("token").toString() : null;
        return processValidation(authHeader, bodyToken);
    }

    private ResponseEntity<Map<String, Object>> processValidation(String authHeader, String bodyToken) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else if (bodyToken != null && !bodyToken.isBlank()) {
            token = bodyToken.trim();
        }

        if (token == null || token.isBlank() || token.equalsIgnoreCase("invalid") || token.contains("invalid")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "valid", false,
                    "error", "Unauthorized",
                    "message", "Invalid, expired, or missing Bearer token"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "username", "admin",
                "role", "ADMIN",
                "userId", 1,
                "message", "Token is authentic and verified"
        ));
    }
}


package com.example.mock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@SpringBootApplication
@RestController
public class MockServicesApplication {

    @Value("${service.name:Mock SOA Service}")
    private String serviceName;

    @Value("${server.port:8080}")
    private int port;

    public static void main(String[] args) {
        SpringApplication.run(MockServicesApplication.class, args);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", serviceName,
                "port", port,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}


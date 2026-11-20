package com.example.reporting.client;

import com.example.reporting.dto.AuthValidationResponse;
import com.example.reporting.exception.ServiceUnavailableException;
import com.example.reporting.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestClient authRestClient;

    /**
     * Validates Bearer token by calling Auth Service /api/auth/validate
     * Supports both Bearer header and JSON body token verification
     */
    public AuthValidationResponse validateToken(String rawToken) {
        if (rawToken == null || rawToken.trim().isEmpty()) {
            throw new UnauthorizedException("Missing authentication token");
        }

        String bearerHeader = rawToken.startsWith("Bearer ") ? rawToken : "Bearer " + rawToken;
        String cleanToken = rawToken.startsWith("Bearer ") ? rawToken.substring(7).trim() : rawToken.trim();

        try {
            log.debug("Calling Auth Service to validate token...");
            AuthValidationResponse response = authRestClient.get()
                    .uri("/api/auth/validate")
                    .header(HttpHeaders.AUTHORIZATION, bearerHeader)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Auth Service rejected token with status: {}", res.getStatusCode());
                        throw new UnauthorizedException("Invalid or expired authentication token");
                    })
                    .body(AuthValidationResponse.class);

            if (response == null || !response.isValid()) {
                throw new UnauthorizedException("Token validation failed by Auth Service");
            }

            return response;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new UnauthorizedException("Invalid or expired authentication token: " + e.getMessage());
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to Auth Service: {}", e.getMessage());
            throw new ServiceUnavailableException("Auth Service", "Connection timeout or service is offline");
        } catch (Exception e) {
            log.error("Unexpected error validating token with Auth Service: {}", e.getMessage());
            // Fallback: try POST /api/auth/validate with body
            try {
                AuthValidationResponse postResponse = authRestClient.post()
                        .uri("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, bearerHeader)
                        .body(Map.of("token", cleanToken))
                        .retrieve()
                        .body(AuthValidationResponse.class);
                if (postResponse != null && postResponse.isValid()) {
                    return postResponse;
                }
            } catch (Exception ignored) {
                // Ignore fallback exception and rethrow appropriate error
            }
            throw new UnauthorizedException("Authentication failed: " + e.getMessage());
        }
    }
}


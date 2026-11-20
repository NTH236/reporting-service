package com.example.reporting.config;

import com.example.reporting.client.AuthServiceClient;
import com.example.reporting.dto.AuthValidationResponse;
import com.example.reporting.dto.ErrorResponse;
import com.example.reporting.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    public static final String AUTH_USER_ATTR = "AUTHENTICATED_USER";
    public static final String RAW_TOKEN_ATTR = "RAW_BEARER_TOKEN";

    private final AuthServiceClient authServiceClient;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Whitelist Swagger UI, OpenAPI documentation, Actuator, H2 console, and root/error
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/h2-console")
                || path.startsWith("/actuator")
                || path.equals("/error")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized access to {}: Missing or invalid Authorization header", request.getRequestURI());
            writeUnauthorizedResponse(response, "Authorization header with Bearer token is required", request.getRequestURI());
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            writeUnauthorizedResponse(response, "Bearer token is empty", request.getRequestURI());
            return;
        }

        try {
            AuthValidationResponse validation = authServiceClient.validateToken(token);
            if (validation == null || !validation.isValid()) {
                writeUnauthorizedResponse(response, "Invalid or expired token", request.getRequestURI());
                return;
            }

            // Store user and token in request attributes for downstream usage
            request.setAttribute(AUTH_USER_ATTR, validation);
            request.setAttribute(RAW_TOKEN_ATTR, authHeader);

            filterChain.doFilter(request, response);
        } catch (UnauthorizedException e) {
            log.warn("Auth Service rejected token: {}", e.getMessage());
            writeUnauthorizedResponse(response, e.getMessage(), request.getRequestURI());
        } catch (Exception e) {
            log.error("Authentication error on {}: {}", request.getRequestURI(), e.getMessage());
            writeUnauthorizedResponse(response, "Authentication verification failed: " + e.getMessage(), request.getRequestURI());
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message, String path) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(message)
                .path(path)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}


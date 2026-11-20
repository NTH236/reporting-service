package com.example.reporting.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthValidationResponse {

    @JsonAlias({"valid", "isValid", "authenticated", "active"})
    private boolean valid;

    private String username;

    private String role;

    @JsonAlias({"user_id", "userId", "id"})
    private Long userId;

    private String message;
}


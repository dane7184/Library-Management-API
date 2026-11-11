package com.library.auth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/** Raw (snake_case) body of Keycloak's token endpoint. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KeycloakTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn
) {
    public TokenResponse toTokenResponse() {
        return new TokenResponse(accessToken, tokenType, expiresIn, refreshToken, refreshExpiresIn);
    }
}

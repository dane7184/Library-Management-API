package com.library.auth.dto;

/** Token pair returned to clients. Send accessToken as "Authorization: Bearer ..." on API calls. */
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn
) {
}

package com.library.auth;

/** Bad credentials, or an expired / revoked refresh token. Mapped to 401. */
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}

package com.library.auth;

/** Keycloak could not be reached or answered unexpectedly. Mapped to 503. */
public class IdentityProviderUnavailableException extends RuntimeException {
    public IdentityProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

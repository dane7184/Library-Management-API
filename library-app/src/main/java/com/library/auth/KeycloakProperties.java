package com.library.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Where to obtain tokens: Keycloak's OpenID Connect token endpoint and the public client to use. */
@ConfigurationProperties(prefix = "library.keycloak")
public record KeycloakProperties(String tokenUri, String clientId) {
}

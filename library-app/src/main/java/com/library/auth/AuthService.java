package com.library.auth;

import com.library.auth.dto.KeycloakTokenResponse;
import com.library.auth.dto.LoginRequest;
import com.library.auth.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * Exchanges credentials for Keycloak tokens (OAuth2 password grant against the public "library-api" client)
 * so API clients only need to talk to this application.
 */
@Slf4j
@Service
public class AuthService {

    private final RestClient restClient;
    private final KeycloakProperties properties;

    public AuthService(RestClient.Builder builder, KeycloakProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = builder.requestFactory(requestFactory).build();
        this.properties = properties;
    }

    public TokenResponse login(LoginRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.clientId());
        form.add("username", request.username());
        form.add("password", request.password());
        form.add("scope", "openid");
        return requestToken(form, "Invalid username or password");
    }

    public TokenResponse refresh(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", properties.clientId());
        form.add("refresh_token", refreshToken);
        return requestToken(form, "Refresh token is invalid or expired, please log in again");
    }

    private TokenResponse requestToken(MultiValueMap<String, String> form, String rejectedMessage) {
        KeycloakTokenResponse body;
        try {
            body = restClient.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest ex) {
            // Keycloak answers 401 invalid_grant for bad credentials and 400 invalid_grant for
            // expired/revoked refresh tokens, disabled accounts or accounts with pending required actions.
            log.debug("Token request rejected by Keycloak: {}", ex.getResponseBodyAsString());
            throw new AuthenticationFailedException(rejectedMessage);
        } catch (RestClientException ex) {
            log.error("Token request to {} failed", properties.tokenUri(), ex);
            throw new IdentityProviderUnavailableException("Identity provider is unavailable, try again later", ex);
        }
        if (body == null || body.accessToken() == null) {
            throw new IdentityProviderUnavailableException("Identity provider returned no token", null);
        }
        return body.toTokenResponse();
    }
}

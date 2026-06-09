package com.DSfinal.broker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class Auth0TokenService {

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.client-id}")
    private String clientId;

    @Value("${auth0.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // cached token and expiry
    private volatile String cachedToken;
    private volatile Instant tokenExpiry = Instant.EPOCH;
    private final ReentrantLock lock = new ReentrantLock();

    public String getAccessToken() {
        // return cached token if still valid (with a 30s safety margin)
        Instant now = Instant.now();
        if (cachedToken != null && now.isBefore(tokenExpiry.minusSeconds(30))) {
            return cachedToken;
        }

        lock.lock();
        try {
            // double-check after acquiring lock
            now = Instant.now();
            if (cachedToken != null && now.isBefore(tokenExpiry.minusSeconds(30))) {
                return cachedToken;
            }

            String url = "https://" + domain + "/oauth/token";

            Map<String, String> request = Map.of(
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "audience", "https://supplier-api",
                    "grant_type", "client_credentials"
            );

            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(url, request, TokenResponse.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                System.out.println("Auth0 token endpoint returned non-OK: " + response.getStatusCode());
                return null;
            }

            String token = response.getBody().getAccessToken();
            long expiresIn = response.getBody().getExpiresIn() <= 0 ? 60L : response.getBody().getExpiresIn();
            cachedToken = token;
            tokenExpiry = Instant.now().plusSeconds(expiresIn);
            System.out.println("Obtained Auth0 token, expires in: " + expiresIn + "s");
            System.out.println(token);
            return cachedToken;
        } catch (Exception ex) {
            System.out.println("Failed to fetch Auth0 token: " + ex.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
    }

    static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private long expiresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        // setters for Jackson
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }
    }
}
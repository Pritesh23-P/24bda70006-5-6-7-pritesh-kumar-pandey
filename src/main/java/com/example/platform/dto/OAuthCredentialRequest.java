package com.example.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OAuthCredentialRequest {

    @NotBlank(message = "Provider is required")
    @Pattern(regexp = "^(TWITTER|LINKEDIN|FACEBOOK|GOOGLE)$", message = "Provider must be TWITTER, LINKEDIN, FACEBOOK, or GOOGLE")
    private String provider;

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Client Secret is required")
    private String clientSecret;

    @NotBlank(message = "Access Token is required")
    private String accessToken;

    private String refreshToken;

    public OAuthCredentialRequest() {
    }

    public OAuthCredentialRequest(String provider, String clientId, String clientSecret, String accessToken, String refreshToken) {
        this.provider = provider;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

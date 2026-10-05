package com.example.platform.dto;

import java.time.LocalDateTime;

public class OAuthCredentialResponse {

    private Long id;
    private String provider;
    private String clientId;
    private String maskedClientSecret;
    private String maskedAccessToken;
    private String maskedRefreshToken;
    private String decryptedClientSecret; // Populated only when authorized
    private String decryptedAccessToken;  // Populated only when authorized
    private LocalDateTime createdAt;

    public OAuthCredentialResponse() {
    }

    public OAuthCredentialResponse(Long id, String provider, String clientId, String maskedClientSecret, String maskedAccessToken, String maskedRefreshToken, LocalDateTime createdAt) {
        this.id = id;
        this.provider = provider;
        this.clientId = clientId;
        this.maskedClientSecret = maskedClientSecret;
        this.maskedAccessToken = maskedAccessToken;
        this.maskedRefreshToken = maskedRefreshToken;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMaskedClientSecret() {
        return maskedClientSecret;
    }

    public void setMaskedClientSecret(String maskedClientSecret) {
        this.maskedClientSecret = maskedClientSecret;
    }

    public String getMaskedAccessToken() {
        return maskedAccessToken;
    }

    public void setMaskedAccessToken(String maskedAccessToken) {
        this.maskedAccessToken = maskedAccessToken;
    }

    public String getMaskedRefreshToken() {
        return maskedRefreshToken;
    }

    public void setMaskedRefreshToken(String maskedRefreshToken) {
        this.maskedRefreshToken = maskedRefreshToken;
    }

    public String getDecryptedClientSecret() {
        return decryptedClientSecret;
    }

    public void setDecryptedClientSecret(String decryptedClientSecret) {
        this.decryptedClientSecret = decryptedClientSecret;
    }

    public String getDecryptedAccessToken() {
        return decryptedAccessToken;
    }

    public void setDecryptedAccessToken(String decryptedAccessToken) {
        this.decryptedAccessToken = decryptedAccessToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

package com.example.platform.controller;

import com.example.platform.common.ApiResponse;
import com.example.platform.dto.OAuthCredentialRequest;
import com.example.platform.dto.OAuthCredentialResponse;
import com.example.platform.service.OAuthCredentialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credentials")
public class CredentialController {

    private final OAuthCredentialService credentialService;

    public CredentialController(OAuthCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OAuthCredentialResponse>> storeCredentials(
            @Valid @RequestBody OAuthCredentialRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        OAuthCredentialResponse response = credentialService.storeCredentials(request, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.success("OAuth credentials encrypted and stored safely", response), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OAuthCredentialResponse>>> getUserCredentials(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<OAuthCredentialResponse> responses = credentialService.getUserCredentials(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Credentials retrieved (masked)", responses));
    }

    @GetMapping("/{id}/decrypt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OAuthCredentialResponse>> getDecryptedCredential(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        OAuthCredentialResponse response = credentialService.getDecryptedCredential(id, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Credentials decrypted securely with AES-256", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> deleteCredential(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        credentialService.deleteCredential(id, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Credential deleted", "Credential ID " + id + " has been removed"));
    }
}

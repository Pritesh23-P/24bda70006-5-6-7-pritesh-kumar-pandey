package com.example.platform.service;

import com.example.platform.dto.OAuthCredentialRequest;
import com.example.platform.dto.OAuthCredentialResponse;
import com.example.platform.exception.DuplicateResourceException;
import com.example.platform.exception.ResourceNotFoundException;
import com.example.platform.model.OAuthCredential;
import com.example.platform.model.User;
import com.example.platform.repository.OAuthCredentialRepository;
import com.example.platform.repository.UserRepository;
import com.example.platform.security.AesEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OAuthCredentialService {

    private static final Logger log = LoggerFactory.getLogger(OAuthCredentialService.class);

    private final OAuthCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final AesEncryptionService aesEncryptionService;

    public OAuthCredentialService(OAuthCredentialRepository credentialRepository,
                                  UserRepository userRepository,
                                  AesEncryptionService aesEncryptionService) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.aesEncryptionService = aesEncryptionService;
    }

    @Transactional
    public OAuthCredentialResponse storeCredentials(OAuthCredentialRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (credentialRepository.findByUserAndProvider(user, request.getProvider()).isPresent()) {
            throw new DuplicateResourceException("Credentials for provider '" + request.getProvider() + "' already exist for this user");
        }

        // AES-256-GCM encryption of sensitive fields
        String encryptedSecret = aesEncryptionService.encrypt(request.getClientSecret());
        String encryptedAccess = aesEncryptionService.encrypt(request.getAccessToken());
        String encryptedRefresh = request.getRefreshToken() != null ? aesEncryptionService.encrypt(request.getRefreshToken()) : null;

        OAuthCredential credential = new OAuthCredential(
                user,
                request.getProvider().toUpperCase(),
                request.getClientId(),
                encryptedSecret,
                encryptedAccess,
                encryptedRefresh,
                LocalDateTime.now().plusMonths(3)
        );

        OAuthCredential saved = credentialRepository.save(credential);
        log.info("Securely stored AES-encrypted credentials for user {} and provider {}", username, saved.getProvider());

        return mapToMaskedResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OAuthCredentialResponse> getUserCredentials(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return credentialRepository.findByUser(user).stream()
                .map(this::mapToMaskedResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OAuthCredentialResponse getDecryptedCredential(Long id, String username, boolean isAdmin) {
        OAuthCredential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with ID: " + id));

        if (!credential.getUser().getUsername().equals(username) && !isAdmin) {
            throw new ResourceNotFoundException("Credential not found with ID: " + id);
        }

        OAuthCredentialResponse response = mapToMaskedResponse(credential);
        // Decrypt on authorized request
        response.setDecryptedClientSecret(aesEncryptionService.decrypt(credential.getEncryptedClientSecret()));
        response.setDecryptedAccessToken(aesEncryptionService.decrypt(credential.getEncryptedAccessToken()));

        log.info("Decrypted sensitive OAuth credentials for credential ID {} requested by {}", id, username);
        return response;
    }

    @Transactional
    public void deleteCredential(Long id, String username, boolean isAdmin) {
        OAuthCredential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with ID: " + id));

        if (!credential.getUser().getUsername().equals(username) && !isAdmin) {
            throw new ResourceNotFoundException("Credential not found with ID: " + id);
        }

        credentialRepository.delete(credential);
        log.info("Deleted OAuth credential ID {}", id);
    }

    private OAuthCredentialResponse mapToMaskedResponse(OAuthCredential credential) {
        // Decrypt briefly to compute safe masked representation, never returning plain text by default
        String rawSecret = aesEncryptionService.decrypt(credential.getEncryptedClientSecret());
        String rawToken = aesEncryptionService.decrypt(credential.getEncryptedAccessToken());

        return new OAuthCredentialResponse(
                credential.getId(),
                credential.getProvider(),
                credential.getClientId(),
                aesEncryptionService.mask(rawSecret),
                aesEncryptionService.mask(rawToken),
                credential.getEncryptedRefreshToken() != null ? "****************" : "N/A",
                credential.getCreatedAt()
        );
    }
}

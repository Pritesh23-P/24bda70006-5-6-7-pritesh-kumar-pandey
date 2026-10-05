package com.example.platform.repository;

import com.example.platform.model.OAuthCredential;
import com.example.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthCredentialRepository extends JpaRepository<OAuthCredential, Long> {
    List<OAuthCredential> findByUser(User user);
    Optional<OAuthCredential> findByUserAndProvider(User user, String provider);
    Optional<OAuthCredential> findByClientId(String clientId);
}

package com.example.platform.service;

import com.example.platform.dto.AuthRequest;
import com.example.platform.dto.AuthResponse;
import com.example.platform.dto.RefreshTokenRequest;
import com.example.platform.dto.RegisterRequest;
import com.example.platform.exception.BadRequestException;
import com.example.platform.exception.DuplicateResourceException;
import com.example.platform.exception.UnauthorizedException;
import com.example.platform.model.RefreshToken;
import com.example.platform.model.Role;
import com.example.platform.model.User;
import com.example.platform.repository.RefreshTokenRepository;
import com.example.platform.repository.RoleRepository;
import com.example.platform.repository.UserRepository;
import com.example.platform.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenDurationMs;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        Set<Role> roles = new HashSet<>();
        String requestedRole = (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN"))
                ? "ROLE_ADMIN"
                : "ROLE_USER";

        Role role = roleRepository.findByName(requestedRole)
                .orElseGet(() -> roleRepository.save(new Role(requestedRole)));
        roles.add(role);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("Registered new user: {} with role: {}", savedUser.getUsername(), requestedRole);

        List<String> roleNames = savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        String accessToken = jwtService.generateTokenFromUsername(savedUser.getUsername(), roleNames);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtService.getExpirationMs() / 1000,
                savedUser.getUsername(),
                savedUser.getEmail(),
                new HashSet<>(roleNames)
        );
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        String accessToken = jwtService.generateToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user);

        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        log.info("User {} logged in successfully", user.getUsername());
        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtService.getExpirationMs() / 1000,
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new BadRequestException("Refresh token does not exist"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token was revoked. Please log in again.");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");
        }

        User user = refreshToken.getUser();
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        String newAccessToken = jwtService.generateTokenFromUsername(user.getUsername(), roles);

        // Refresh token rotation
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        RefreshToken rotatedToken = refreshTokenRepository.save(refreshToken);

        log.info("Refreshed and rotated token for user: {}", user.getUsername());

        return new AuthResponse(
                newAccessToken,
                rotatedToken.getToken(),
                jwtService.getExpirationMs() / 1000,
                user.getUsername(),
                user.getEmail(),
                new HashSet<>(roles)
        );
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            refreshTokenRepository.findByUser(user).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                log.info("Revoked refresh token on logout for user: {}", username);
            });
        });
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> {
                    RefreshToken token = new RefreshToken();
                    token.setUser(user);
                    return token;
                });

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }
}

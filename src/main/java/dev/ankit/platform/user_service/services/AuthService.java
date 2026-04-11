package dev.ankit.platform.user_service.services;

import dev.ankit.platform.user_service.domain.AuthProvider;
import dev.ankit.platform.user_service.domain.RefreshToken;
import dev.ankit.platform.user_service.domain.User;
import dev.ankit.platform.user_service.dto.AuthResponse;
import dev.ankit.platform.user_service.dto.LoginRequest;
import dev.ankit.platform.user_service.dto.RegisterRequest;
import dev.ankit.platform.user_service.repository.RefreshTokenRepository;
import dev.ankit.platform.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(
                request.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(
                        request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .verified(false)
                .build();

        User saved = userRepository.save(user);
        return generateAuthResponse(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getPassword() == null) {
            throw new RuntimeException(
                    "Please login with " +
                            user.getProvider().name());
        }

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Purane refresh tokens delete karo
        refreshTokenRepository.deleteByUser(user);

        return generateAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        // DB mein dhundho
        RefreshToken stored = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid refresh token"));

        // Revoked check
        if (stored.isRevoked()) {
            throw new RuntimeException(
                    "Refresh token revoked");
        }

        // Expired check
        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new RuntimeException(
                    "Refresh token expired — please login again");
        }

        // Purana token revoke karo
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // Naya token generate karo
        User user = stored.getUser();
        return generateAuthResponse(user);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    public AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());

        String refreshToken = jwtService.generateRefreshToken(
                user.getId());

        // Refresh token DB mein save karo
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(Instant.now().plusMillis(
                        refreshTokenExpiration))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole())
                .accessTokenExpiresIn(900)    // 15 min
                .refreshTokenExpiresIn(604800) // 7 days
                .build();
    }
}

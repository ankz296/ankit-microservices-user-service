package dev.ankit.platform.user_service.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ankit.platform.user_service.domain.AuthProvider;
import dev.ankit.platform.user_service.domain.User;
import dev.ankit.platform.user_service.dto.AuthResponse;
import dev.ankit.platform.user_service.repository.RefreshTokenRepository;
import dev.ankit.platform.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialAuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthService authService;
    private final RestTemplate restTemplate;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String auth0ClientId;

    @Value("${spring.security.oauth2.client.registration.auth0.client-secret}")
    private String auth0ClientSecret;
    @Value("${spring.security.oauth2.client.registration.auth0.redirect-uri}")
    private String auth0RedirectUri;

    @Transactional
    public AuthResponse handleGoogleCallback(String code) {

        // Step 1: Code se Google access token lo
        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params =
                new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = restTemplate
                .postForObject(tokenUrl, params, Map.class);

        String idToken = (String) tokenResponse.get("id_token");

        // Step 2: ID token se user info extract karo
        Map<String, Object> userInfo =
                decodeGoogleIdToken(idToken);

        String email      = (String) userInfo.get("email");
        String name       = (String) userInfo.get("name");
        String providerId = (String) userInfo.get("sub");

        log.info("Google user: email={}", email);

        // Step 3: DB check + user find/create
        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            user = User.builder()
                    .name(name)
                    .email(email)
                    .password(null)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .verified(true)
                    .build();
            user = userRepository.save(user);
            log.info("New Google user created: {}", email);
        } else {
            // Existing user — provider update karo
            if (user.getProvider() == AuthProvider.LOCAL) {
                // Local user Google se bhi login kar raha hai
                user.setProviderId(providerId);
                user = userRepository.save(user);
            }
            log.info("Existing user Google login: {}", email);
        }

        // Purane tokens delete
        refreshTokenRepository.deleteByUser(user);

        // Step 4: APNA JWT generate karo
        return authService.generateAuthResponse(user);
    }

    private Map<String, Object> decodeGoogleIdToken(
            String idToken) {
        // ID token decode karo (middle part base64)
        String[] parts = idToken.split("\\.");
        byte[] decoded = Base64.getUrlDecoder()
                .decode(parts[1]);
        String json = new String(decoded);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to decode Google token", e);
        }
    }

    public AuthResponse handleAuth0Callback(String code) {
        String tokenUrl =
                "https://dev-mndhi8nb1tdg4f1v.us.auth0.com/oauth/token";

        MultiValueMap<String, String> params =
                new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", auth0ClientId);
        params.add("client_secret", auth0ClientSecret);
        params.add("redirect_uri", auth0RedirectUri);
        params.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = restTemplate
                .postForObject(tokenUrl, params, Map.class);

        String idToken = (String) tokenResponse.get("id_token");

        // Same Google jaisa decode + user find/create + JWT
        Map<String, Object> userInfo =
                decodeGoogleIdToken(idToken); // Same method works!

        String email      = (String) userInfo.get("email");
        String name       = (String) userInfo.get("name");
        String providerId = (String) userInfo.get("sub");

        log.info("Auth0 user: email={}", email);

        // Step 3: DB check + user find/create
        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            user = User.builder()
                    .name(name)
                    .email(email)
                    .password(null)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .verified(true)
                    .build();
            user = userRepository.save(user);
            log.info("New Auth0 user created: {}", email);
        } else {
            // Existing user — provider update karo
            log.info("Existing user social login: {}", email);
            if (user.getProvider() == AuthProvider.LOCAL) {
                // Local user Google se bhi login kar raha hai
                user.setProviderId(providerId);
                user = userRepository.save(user);
            }
            log.info("Existing user Auth0 login: {}", email);
        }

        // Purane tokens delete
        refreshTokenRepository.deleteByUser(user);

        // Step 4: APNA JWT generate karo
        return authService.generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse handleSocialLogin(
            OAuth2User oAuth2User,
            String providerName) {

        // Google/Auth0 se data extract karo
        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        // providerId null check — Google uses "sub"
        if (providerId == null) {
            providerId = oAuth2User.getName();
        }

        AuthProvider provider = AuthProvider
                .valueOf(providerName);

        log.info("Social login — provider={}, email={}",
                providerName, email);

        // Email DB mein check karo
        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            // Naya user banao
            log.info("New social user — creating: {}", email);
            user = User.builder()
                    .name(name)
                    .email(email)
                    .password(null)      // Social = no password
                    .provider(provider)
                    .providerId(providerId)
                    .verified(true)      // Social = already verified
                    .build();
            user = userRepository.save(user);

        } else {
            // Existing user — provider update karo
            //log.info("Existing user social login: {}", email);
            if (user.getProvider() == AuthProvider.LOCAL) {
                // Local user Google se bhi login kar raha hai
                user.setProviderId(providerId);
                user = userRepository.save(user);
            }
        }

        // Purane refresh tokens delete
        refreshTokenRepository.deleteByUser(user);

        // APNA JWT generate karo
        return authService.generateAuthResponse(user);
    }
}

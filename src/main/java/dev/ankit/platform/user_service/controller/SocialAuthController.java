package dev.ankit.platform.user_service.controller;


import dev.ankit.platform.user_service.dto.AuthResponse;
import dev.ankit.platform.user_service.services.SocialAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class SocialAuthController {

    private final SocialAuthService socialAuthService;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;
    @Value("${spring.security.oauth2.client.registration.auth0.redirect-uri}")
    private String auth0RedirectUri;

    // Step 1: Google login URL generate karo
    @GetMapping("/google")
    public ResponseEntity<Map<String, String>> googleLogin(
            @Value("${spring.security.oauth2.client.registration.google.client-id}")
            String clientId) {

        String googleAuthUrl =
                "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=" + clientId +
                        "&redirect_uri=" + googleRedirectUri +
                        "&response_type=code" +
                        "&scope=openid%20profile%20email";

        return ResponseEntity.ok(
                Map.of("loginUrl", googleAuthUrl));
    }

    // Step 2: Google callback — code receive karo
    @GetMapping("/google/callback")
    public ResponseEntity<AuthResponse> googleCallback(
            @RequestParam("code") String code) {

        log.info("Google callback received");
        return ResponseEntity.ok(
                socialAuthService.handleGoogleCallback(code));
    }

    // SocialAuthController mein add karo
    @GetMapping("/auth0")
    public ResponseEntity<Map<String, String>> auth0Login(
            @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
                                                              String clientId) {
        String auth0Url =
                "https://dev-mndhi8nb1tdg4f1v.us.auth0.com/authorize?" +
                        "client_id=" + clientId +
                        "&redirect_uri=" + auth0RedirectUri +
                        "&response_type=code" +
                        "&scope=openid%20profile%20email";

        return ResponseEntity.ok(
                Map.of("loginUrl", auth0Url));
    }

    @GetMapping("/auth0/callback")
    public ResponseEntity<AuthResponse> auth0Callback(
            @RequestParam("code") String code) {
        return ResponseEntity.ok(
                socialAuthService.handleAuth0Callback(code));
    }

//    // Auth0 Login
//    @GetMapping("/auth0/callback")
//    public ResponseEntity<AuthResponse> auth0Callback(
//            @AuthenticationPrincipal OAuth2User oAuth2User) {
//        Object email = oAuth2User.getAttribute("email");
//        assert email != null;
//
//        log.info("Auth0 callback — email={}", email);
//
//        return ResponseEntity.ok(
//                socialAuthService.handleSocialLogin(
//                        oAuth2User, "AUTH0"));
//    }
}

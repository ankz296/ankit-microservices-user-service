package dev.ankit.platform.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String email;
    private String role;
    private String tokenType = "Bearer";
    private long accessTokenExpiresIn;   // seconds mein
    private long refreshTokenExpiresIn;  // seconds mein
}

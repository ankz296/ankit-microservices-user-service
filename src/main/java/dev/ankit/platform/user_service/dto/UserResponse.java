package dev.ankit.platform.user_service.dto;


import java.time.Instant;

public record UserResponse(Long id, String name, String email, Instant createdAt) {
}
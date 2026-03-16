package dev.ankit.platform.user_service.dto;


import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, Instant createdAt) {
}
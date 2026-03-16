package dev.ankit.platform.user_service.dto.internal;

import java.util.UUID;

public record UserInternalDto(
        UUID userId,
        boolean active
) {
}

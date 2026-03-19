package dev.ankit.platform.user_service.controller.internal;


import dev.ankit.platform.user_service.dto.UserResponse;
import dev.ankit.platform.user_service.dto.internal.UserInternalDto;
import dev.ankit.platform.user_service.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/internal/users")
public class UserInternalController {

    private final UserService userService;

    public UserInternalController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInternalDto> getUser(@PathVariable UUID userId) {

        log.info("Internal request: fetch user userId={}", userId);

        UserResponse user = userService.get(userId);

        log.debug("Internal user fetched userId={}", userId);

        return ResponseEntity.ok(new UserInternalDto(user.id(), true));
    }
}

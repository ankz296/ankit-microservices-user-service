package dev.ankit.platform.user_service.controller.internal;


import dev.ankit.platform.user_service.dto.UserResponse;
import dev.ankit.platform.user_service.dto.internal.UserInternalDto;
import dev.ankit.platform.user_service.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
public class UserInternalController {

    private final UserService userService;

    public UserInternalController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInternalDto> getUser(@PathVariable UUID userId) {
        // You can adapt this based on your existing service methods
        UserResponse user = userService.get(userId); // should throw if not found
        return ResponseEntity.ok(new UserInternalDto(user.id(), true));
    }
}

package dev.ankit.platform.user_service.controller;

import dev.ankit.platform.user_service.dto.UserRequest;
import dev.ankit.platform.user_service.dto.UserResponse;
import dev.ankit.platform.user_service.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest req) {

        log.info("Create user request email={}", req.email());

        UserResponse response = service.create(req);

        log.info("User created successfully userId={}", response.id());

        return response;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public UserResponse get(@PathVariable UUID id) {

        log.info("Fetching user id={}", id);

        UserResponse response = service.get(id);

        log.debug("User fetched id={}", id);

        return response;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public List<UserResponse> list() {

        log.info("Fetching all users");

        List<UserResponse> response = service.list();

        log.info("Users fetched count={}", response.size());

        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserRequest req) {

        log.info("Updating user id={}", id);

        UserResponse response = service.update(id, req);

        log.info("User updated successfully id={}", id);

        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {

        log.warn("Deleting user id={}", id);

        service.delete(id);

        log.info("User deleted id={}", id);
    }
}
package dev.ankit.platform.user_service.controller;

import dev.ankit.platform.user_service.dto.UserRequest;
import dev.ankit.platform.user_service.dto.UserResponse;
import dev.ankit.platform.user_service.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<UserResponse> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
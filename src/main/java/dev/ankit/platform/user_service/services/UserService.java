package dev.ankit.platform.user_service.services;


import dev.ankit.platform.user_service.domain.User;
import dev.ankit.platform.user_service.dto.UserRequest;
import dev.ankit.platform.user_service.dto.UserResponse;
import dev.ankit.platform.user_service.exception.BusinessException;
import dev.ankit.platform.user_service.exception.ResourceNotFoundException;
import dev.ankit.platform.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public UserResponse create(UserRequest req) {

        log.info("Creating user email={}", req.email());

        if (repo.existsByEmail(req.email())) {
            log.warn("User creation failed - email already exists email={}", req.email());
            throw new BusinessException("Email already registered");
        }

        User u = User.builder()
                .name(req.name())
                .email(req.email())
                .createdAt(Instant.now())
                .build();

        User saved = repo.save(u);

        log.info("User created successfully userId={}", saved.getId());

        return toResp(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {

        log.debug("Fetching user id={}", id);

        User u = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found id={}", id);
                    return new ResourceNotFoundException("User not found: " + id);
                });

        return toResp(u);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {

        log.debug("Fetching all users");

        List<UserResponse> users = repo.findAll()
                .stream()
                .map(this::toResp)
                .toList();

        log.info("Users fetched count={}", users.size());

        return users;
    }

    public UserResponse update(UUID id, UserRequest req) {

        log.info("Updating user id={}", id);

        User u = repo.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for update id={}", id);
                    return new ResourceNotFoundException("User not found: " + id);
                });

        if (!u.getEmail().equals(req.email()) && repo.existsByEmail(req.email())) {
            log.warn("User update failed - email already exists email={}", req.email());
            throw new BusinessException("Email already registered");
        }

        u.setName(req.name());
        u.setEmail(req.email());

        User updated = repo.save(u);

        log.info("User updated successfully id={}", id);

        return toResp(updated);
    }

    public void delete(UUID id) {

        log.warn("Deleting user id={}", id);

        if (!repo.existsById(id)) {
            log.warn("User not found for delete id={}", id);
            throw new ResourceNotFoundException("User not found: " + id);
        }

        repo.deleteById(id);

        log.info("User deleted successfully id={}", id);
    }

    private UserResponse toResp(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
    }
}
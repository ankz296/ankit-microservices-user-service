package dev.ankit.platform.user_service.services;


import dev.ankit.platform.user_service.domain.User;
import dev.ankit.platform.user_service.dto.UserRequest;
import dev.ankit.platform.user_service.dto.UserResponse;
import dev.ankit.platform.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public UserResponse create(UserRequest req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User u = User.builder()
                .name(req.name())
                .email(req.email())
                .createdAt(Instant.now())
                .build();
        User saved = repo.save(u);
        return toResp(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        User u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResp(u);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return repo.findAll().stream().map(this::toResp).toList();
    }

    public UserResponse update(Long id, UserRequest req) {
        User u = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!u.getEmail().equals(req.email()) && repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        u.setName(req.name());
        u.setEmail(req.email());
        return toResp(repo.save(u));
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("User not found");
        repo.deleteById(id);
    }

    private UserResponse toResp(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
    }
}
package com.corewise.modernization.service;

import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ConflictException;
import com.corewise.modernization.common.ResourceNotFoundException;
import com.corewise.modernization.domain.model.User;
import com.corewise.modernization.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates and looks up our team's user accounts. Passwords are hashed here with
 * BCrypt before they ever reach the database, and we never hand back the hash.
 */
@Service
public class UserService {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_ENGINEER = "engineer";
    public static final String STATUS_ACTIVE = "active";

    private static final Set<String> ALLOWED_ROLES = Set.of(ROLE_ADMIN, ROLE_ENGINEER);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User create(String email, String fullName, String rawPassword, String role) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (!ALLOWED_ROLES.contains(role)) {
            throw new BadRequestException("Role must be either admin or engineer.");
        }
        if (users.existsByEmail(normalizedEmail)) {
            throw new ConflictException("A user with that email already exists.");
        }
        User user = new User(normalizedEmail, fullName.trim(),
            passwordEncoder.encode(rawPassword), role, STATUS_ACTIVE);
        return users.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        return users.findAll(Sort.by("email"));
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return users.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("There is no user with id " + id + "."));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return users.findByEmail(email == null ? "" : email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public long count() {
        return users.count();
    }
}

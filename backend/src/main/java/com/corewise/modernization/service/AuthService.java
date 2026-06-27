package com.corewise.modernization.service;

import com.corewise.modernization.common.UnauthorizedException;
import com.corewise.modernization.domain.model.User;
import com.corewise.modernization.security.JwtService;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles signing in. It checks the email and password and, if they match an
 * active account, hands back a signed token. We use the same message for a wrong
 * email and a wrong password on purpose, so the endpoint does not reveal which
 * emails exist.
 */
@Service
public class AuthService {

    private final UserService users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserService users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResult login(String email, String rawPassword) {
        User user = users.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Email or password is not right."));

        if (user.getPasswordHash() == null
            || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Email or password is not right.");
        }
        if (!UserService.STATUS_ACTIVE.equals(user.getStatus())) {
            throw new UnauthorizedException("This account is turned off. Ask an admin to turn it back on.");
        }

        JwtService.IssuedToken token = jwtService.issue(user);
        return new AuthResult(user, token.token(), token.expiresAt());
    }

    /** The signed-in user plus their fresh token. */
    public record AuthResult(User user, String token, Instant expiresAt) {
    }
}

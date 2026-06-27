package com.corewise.modernization.web;

import com.corewise.modernization.service.AuthService;
import com.corewise.modernization.service.UserService;
import com.corewise.modernization.web.dto.LoginRequest;
import com.corewise.modernization.web.dto.LoginResponse;
import com.corewise.modernization.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sign in and find out who you are. Login is open to everyone; who-am-i needs a
 * valid token. Everything else in the app sits behind a token too.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService users;

    public AuthController(AuthService authService, UserService users) {
        this.authService = authService;
        this.users = users;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request.email(), request.password());
        return new LoginResponse(result.token(), result.expiresAt(), UserResponse.from(result.user()));
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        // The token's subject is the user id we put there when we signed it.
        Long userId = Long.valueOf(jwt.getSubject());
        return UserResponse.from(users.getById(userId));
    }
}

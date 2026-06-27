package com.corewise.modernization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.corewise.modernization.common.UnauthorizedException;
import com.corewise.modernization.domain.model.User;
import com.corewise.modernization.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private UserService users;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        users = Mockito.mock(UserService.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtService = Mockito.mock(JwtService.class);
        authService = new AuthService(users, passwordEncoder, jwtService);
    }

    private User activeUser() {
        return new User("dev@corewise.local", "Dev", "hashed", "engineer", "active");
    }

    @Test
    void signsInWithTheRightPassword() {
        User user = activeUser();
        when(users.findByEmail("dev@corewise.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtService.issue(user))
            .thenReturn(new JwtService.IssuedToken("token-123", Instant.now().plusSeconds(60)));

        AuthService.AuthResult result = authService.login("dev@corewise.local", "secret");

        assertThat(result.token()).isEqualTo("token-123");
        assertThat(result.user()).isSameAs(user);
    }

    @Test
    void rejectsTheWrongPassword() {
        User user = activeUser();
        when(users.findByEmail("dev@corewise.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("dev@corewise.local", "wrong"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void rejectsAnUnknownEmail() {
        when(users.findByEmail("nobody@corewise.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("nobody@corewise.local", "secret"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void rejectsADisabledAccount() {
        User disabled = new User("dev@corewise.local", "Dev", "hashed", "engineer", "disabled");
        when(users.findByEmail("dev@corewise.local")).thenReturn(Optional.of(disabled));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login("dev@corewise.local", "secret"))
            .isInstanceOf(UnauthorizedException.class);
    }
}

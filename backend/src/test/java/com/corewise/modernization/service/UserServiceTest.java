package com.corewise.modernization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.common.ConflictException;
import com.corewise.modernization.domain.model.User;
import com.corewise.modernization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        service = new UserService(repository, passwordEncoder);
    }

    @Test
    void createsUserWithLowercasedEmailAndHashedPassword() {
        when(repository.existsByEmail("new@corewise.local")).thenReturn(false);
        when(passwordEncoder.encode("password1")).thenReturn("hashed-pw");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = service.create("New@Corewise.local", "New Person", "password1", "engineer");

        assertThat(created.getEmail()).isEqualTo("new@corewise.local");
        assertThat(created.getPasswordHash()).isEqualTo("hashed-pw");
        assertThat(created.getRole()).isEqualTo("engineer");
        assertThat(created.getStatus()).isEqualTo("active");
    }

    @Test
    void rejectsADuplicateEmail() {
        when(repository.existsByEmail("dupe@corewise.local")).thenReturn(true);

        assertThatThrownBy(() -> service.create("dupe@corewise.local", "Dupe", "password1", "engineer"))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsAnUnknownRole() {
        assertThatThrownBy(() -> service.create("x@corewise.local", "X", "password1", "wizard"))
            .isInstanceOf(BadRequestException.class);
    }
}

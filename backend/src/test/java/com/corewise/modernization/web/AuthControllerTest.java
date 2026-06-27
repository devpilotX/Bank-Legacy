package com.corewise.modernization.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.corewise.modernization.common.UnauthorizedException;
import com.corewise.modernization.domain.model.User;
import com.corewise.modernization.security.RestAccessDeniedHandler;
import com.corewise.modernization.security.RestAuthenticationEntryPoint;
import com.corewise.modernization.security.SecurityConfig;
import com.corewise.modernization.service.AuthService;
import com.corewise.modernization.service.UserService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@TestPropertySource(properties = "app.security.jwt.secret=test-secret-please-change-0123456789abcdef")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @Test
    void rejectsMissingFieldsWith400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("validation_failed"));
    }

    @Test
    void rejectsWrongCredentialsWith401() throws Exception {
        when(authService.login("dev@corewise.local", "wrong"))
            .thenThrow(new UnauthorizedException("Email or password is not right."));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"dev@corewise.local\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    @Test
    void returnsTokenOnSuccess() throws Exception {
        User user = new User("dev@corewise.local", "Dev", "hash", "engineer", "active");
        when(authService.login("dev@corewise.local", "right"))
            .thenReturn(new AuthService.AuthResult(user, "token-123", Instant.now().plusSeconds(60)));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"dev@corewise.local\",\"password\":\"right\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-123"))
            .andExpect(jsonPath("$.user.email").value("dev@corewise.local"));
    }
}

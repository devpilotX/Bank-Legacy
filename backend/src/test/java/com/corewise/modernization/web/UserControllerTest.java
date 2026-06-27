package com.corewise.modernization.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.corewise.modernization.security.RestAccessDeniedHandler;
import com.corewise.modernization.security.RestAuthenticationEntryPoint;
import com.corewise.modernization.security.SecurityConfig;
import com.corewise.modernization.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@TestPropertySource(properties = "app.security.jwt.secret=test-secret-please-change-0123456789abcdef")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void rejectsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void forbidsAnEngineer() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ENGINEER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void allowsAnAdmin() throws Exception {
        when(userService.list()).thenReturn(List.of());
        mockMvc.perform(get("/api/admin/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }
}

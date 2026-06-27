package com.corewise.modernization.web;

import com.corewise.modernization.security.RestAccessDeniedHandler;
import com.corewise.modernization.security.RestAuthenticationEntryPoint;
import com.corewise.modernization.security.SecurityConfig;
import com.corewise.modernization.service.AppInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that /health answers without a login and reports status and version.
 * This is a web-only test, so it does not need a database.
 */
@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@TestPropertySource(properties = "app.security.jwt.secret=test-secret-please-change-0123456789abcdef")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppInfoService appInfo;

    @Test
    void healthIsPublicAndReportsOk() throws Exception {
        when(appInfo.version()).thenReturn("0.0.1-test");

        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.version").value("0.0.1-test"));
    }
}

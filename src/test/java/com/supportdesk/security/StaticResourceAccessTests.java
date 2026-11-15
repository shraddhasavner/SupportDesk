package com.supportdesk.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StaticResourceAccessTests
{
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public access to index.html without authentication")
    void testIndexHtmlPubliclyAccessible() throws Exception
    {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SupportDesk")));
    }

    @Test
    @DisplayName("Public access to login.html without authentication")
    void testLoginHtmlPubliclyAccessible() throws Exception
    {
        mockMvc.perform(get("/login.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SupportDesk - Sign In")));
    }

    @Test
    @DisplayName("Public access to CSS stylesheet without authentication")
    void testCssStylesheetPubliclyAccessible() throws Exception
    {
        mockMvc.perform(get("/css/styles.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("--sidebar-bg")));
    }

    @Test
    @DisplayName("Public access to frontend JavaScript files without authentication")
    void testJavaScriptFilesPubliclyAccessible() throws Exception
    {
        mockMvc.perform(get("/js/config.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AppConfig")));

        mockMvc.perform(get("/js/auth.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AuthService")));

        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("loadDashboardSummary")));

        mockMvc.perform(get("/js/api.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ApiClient")));
    }

    @Test
    @DisplayName("APIs remain strictly protected: Unauthenticated /api/employees, /api/dashboard/summary, and /api/tickets return 401")
    void testApiSecurityPreserved() throws Exception
    {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }
}
package com.soumyajit.apigateway.controller;

import com.soumyajit.apigateway.filter.JwtAuthenticationFilter;
import com.soumyajit.apigateway.filter.RateLimitFilter;
import com.soumyajit.apigateway.model.ApiLog;
import com.soumyajit.apigateway.repository.ApiLogRepository;
import com.soumyajit.apigateway.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; // Import this
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = AdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, RateLimitFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiLogRepository apiLogRepository;

    // --- Mocks to satisfy ApiGatewayApplication.seed() ---
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;
    // ----------------------------------------------------

    @Test
    @DisplayName("GET /admin/logs - Should return paginated logs successfully")
    void listLogs_ShouldReturnPage() throws Exception {
        ApiLog log = new ApiLog();
        log.setId("log-1");
        log.setMethod("GET");
        log.setPath("/api/resource");

        Page<ApiLog> logPage = new PageImpl<>(List.of(log));
        when(apiLogRepository.findAll(any(PageRequest.class))).thenReturn(logPage);

        mockMvc.perform(get("/admin/logs")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("log-1"));
    }

    @Test
    @DisplayName("GET /admin/logs/{id} - Should return 200 when log exists")
    void getLog_Found() throws Exception {
        ApiLog log = new ApiLog();
        log.setId("123");
        when(apiLogRepository.findById("123")).thenReturn(Optional.of(log));

        mockMvc.perform(get("/admin/logs/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    @DisplayName("DELETE /admin/logs/{id} - Should return 204 on successful delete")
    void deleteLog_Success() throws Exception {
        when(apiLogRepository.existsById("123")).thenReturn(true);

        mockMvc.perform(delete("/admin/logs/123"))
                .andExpect(status().isNoContent());
    }
}
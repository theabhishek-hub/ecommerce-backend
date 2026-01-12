package com.abhishek.ecommerce.integration;

import com.abhishek.ecommerce.auth.dto.LoginRequestDto;
import com.abhishek.ecommerce.user.entity.AuthProvider;
import com.abhishek.ecommerce.user.entity.Role;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.UserStatus;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("adminpass"));
        admin.setFullName("Admin");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setProvider(AuthProvider.LOCAL);

        userRepository.save(admin);
    }

    @Test
    void unauthorizedRequest_ShouldReturn401() throws Exception {
        // POST to protected admin endpoint should be unauthorized without token
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginAndAuthorizedRequest_ShouldReturn200() throws Exception {
        // Login to obtain JWT
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("admin@example.com");
        login.setPassword("adminpass");

        String resp = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract token from response JSON (ApiResponse.data.token)
        String token = objectMapper.readTree(resp).path("data").path("token").asText();

        // Use token to perform an admin-protected request (create product) - payload minimal
        mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest()); // Controller will validate payload; authentication should pass so status != 401
    }
}

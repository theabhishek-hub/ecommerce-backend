package com.abhishek.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EcommerceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @org.junit.jupiter.api.Disabled("Integration test disabled - requires full setup with admin user and category data")
    void full_e2e_happy_path() throws Exception {

        /* =====================================================
           1️⃣ SIGN UP USER
         ===================================================== */
        String signupRequest = """
            {
              "email": "user@test.com",
              "password": "Password@123",
              "fullName": "Test User"
            }
        """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequest)
                        .with(csrf()))
                .andExpect(status().isCreated());

        /* =====================================================
           2️⃣ LOGIN USER → GET JWT
         ===================================================== */
        String loginRequest = """
            {
              "email": "user@test.com",
              "password": "Password@123"
            }
        """;

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken =
                objectMapper.readTree(loginResponse).get("accessToken").asText();

        String bearerToken = "Bearer " + accessToken;

        /* =====================================================
           3️⃣ CREATE CATEGORY (ADMIN ENDPOINT)
           (Assumes default admin already exists OR endpoint is protected accordingly)
         ===================================================== */
        String categoryRequest = """
            {
              "name": "Electronics",
              "description": "Electronic items"
            }
        """;

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryRequest)
                        .with(csrf()))
                .andExpect(status().isCreated());

        /* =====================================================
           4️⃣ CREATE PRODUCT
         ===================================================== */
        String productRequest = """
            {
              "name": "iPhone 15",
              "price": 80000,
              "quantity": 10,
              "categoryName": "Electronics"
            }
        """;

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest)
                        .with(csrf()))
                .andExpect(status().isCreated());

        /* =====================================================
           5️⃣ ADD TO CART
         ===================================================== */
        String cartRequest = """
            {
              "productId": 1,
              "quantity": 1
            }
        """;

        mockMvc.perform(post("/api/v1/cart")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartRequest)
                        .with(csrf()))
                .andExpect(status().isOk());

        /* =====================================================
           6️⃣ PLACE ORDER
         ===================================================== */
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearerToken)
                        .with(csrf()))
                .andExpect(status().isCreated());
    }
}

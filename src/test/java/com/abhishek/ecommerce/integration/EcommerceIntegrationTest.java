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
    void full_e2e_happy_path() throws Exception {

        /* =====================================================
           1️⃣ SIGN UP USER
         ===================================================== */
        String signupRequest = """
            {
              "email": "user@test.com",
              "password": "password123",
              "fullName": "Test User"
            }
        """;

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequest))
                .andExpect(status().isCreated());

        /* =====================================================
           2️⃣ LOGIN USER → GET JWT
         ===================================================== */
        String loginRequest = """
            {
              "email": "user@test.com",
              "password": "password123"
            }
        """;

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
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

        mockMvc.perform(post("/categories")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryRequest))
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

        mockMvc.perform(post("/products")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
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

        mockMvc.perform(post("/cart")
                        .header("Authorization", bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartRequest))
                .andExpect(status().isOk());

        /* =====================================================
           6️⃣ PLACE ORDER
         ===================================================== */
        mockMvc.perform(post("/orders")
                        .header("Authorization", bearerToken))
                .andExpect(status().isCreated());
    }
}

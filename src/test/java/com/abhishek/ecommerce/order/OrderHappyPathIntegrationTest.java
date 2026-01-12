package com.abhishek.ecommerce.order;

import com.abhishek.ecommerce.auth.dto.LoginRequestDto;
import com.abhishek.ecommerce.auth.dto.SignupRequestDto;
import com.abhishek.ecommerce.cart.dto.request.AddToCartRequestDto;
import com.abhishek.ecommerce.product.dto.request.ProductCreateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderHappyPathIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void placeOrderHappyPath() throws Exception {
        // Signup user
        SignupRequestDto signup = new SignupRequestDto();
        signup.setEmail("test@example.com");
        signup.setFullName("Test User");
        signup.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        // Login to get token
        LoginRequestDto login = new LoginRequestDto();
        login.setEmail("test@example.com");
        login.setPassword("password");

        String response = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("data").get("token").asText();

        // Add product to cart (assuming product exists or create one, but for simplicity, mock or assume)
        AddToCartRequestDto addCart = new AddToCartRequestDto();
        addCart.setProductId(1L); // Assume product 1 exists
        addCart.setQuantity(1);

        mockMvc.perform(post("/api/v1/cart")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addCart)))
                .andExpect(status().isOk());

        // Place order
        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}
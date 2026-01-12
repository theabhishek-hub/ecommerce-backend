package com.abhishek.ecommerce.user.controller;

import com.abhishek.ecommerce.user.dto.request.UserCreateRequestDto;
import com.abhishek.ecommerce.user.dto.response.UserResponseDto;
import com.abhishek.ecommerce.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_ReturnsList() throws Exception {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(1L);
        dto.setEmail("test@example.com");

        when(userService.getAllUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("test@example.com"));
    }

    @Test
    void createUser_RequiresAuth() throws Exception {
        UserCreateRequestDto req = new UserCreateRequestDto();
        req.setEmail("new@example.com");
        req.setPassword("password");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}

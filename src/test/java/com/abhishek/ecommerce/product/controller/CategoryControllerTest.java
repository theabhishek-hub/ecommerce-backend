package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import com.abhishek.ecommerce.product.dto.request.CategoryCreateRequestDto;
import com.abhishek.ecommerce.product.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getAllCategories_ReturnsList() throws Exception {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(1L);
        dto.setName("Electronics");

        when(categoryService.getAllCategories()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    @Test
    void createCategory_RequiresAuth() throws Exception {
        CategoryCreateRequestDto req = new CategoryCreateRequestDto();
        req.setName("NewCat");

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}

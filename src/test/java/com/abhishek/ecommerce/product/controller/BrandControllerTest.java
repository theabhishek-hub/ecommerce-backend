package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.product.dto.request.BrandCreateRequestDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import com.abhishek.ecommerce.product.service.BrandService;
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

@WebMvcTest(BrandController.class)
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BrandService brandService;

    @Test
    void getAllBrands_ReturnsList() throws Exception {
        BrandResponseDto dto = new BrandResponseDto();
        dto.setId(1L);
        dto.setName("Samsung");

        when(brandService.getAllBrands()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Samsung"));
    }

    @Test
    void createBrand_RequiresAuth() throws Exception {
        BrandCreateRequestDto req = new BrandCreateRequestDto();
        req.setName("NewBrand");

        mockMvc.perform(post("/api/v1/brands")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}

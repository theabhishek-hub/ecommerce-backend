package com.abhishek.ecommerce.inventory.controller;

import com.abhishek.ecommerce.inventory.dto.response.InventoryResponseDto;
import com.abhishek.ecommerce.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void getInventoryByProductId_ReturnsInventory() throws Exception {
        InventoryResponseDto dto = new InventoryResponseDto();
        dto.setProductId(1L);
        dto.setStockQuantity(10);

        when(inventoryService.getInventoryByProductId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/inventory/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stockQuantity").value(10));
    }
}
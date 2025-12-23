package com.abhishek.ecommerce.product.controller;

import com.abhishek.ecommerce.product.entity.Brand;
import com.abhishek.ecommerce.product.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public Brand create(@RequestBody Brand brand) {
        return brandService.create(brand);
    }

    @GetMapping
    public List<Brand> getAll() {
        return brandService.getAll();
    }

    @PutMapping("/{id}")
    public Brand update(@PathVariable Long id, @RequestBody Brand brand) {
        return brandService.update(id, brand);
    }

    @DeleteMapping("/{id}")
    public void deactivate(@PathVariable Long id) {
        brandService.deactivate(id);
    }
}


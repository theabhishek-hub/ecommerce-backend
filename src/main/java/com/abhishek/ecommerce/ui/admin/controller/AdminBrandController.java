package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.product.service.BrandService;
import com.abhishek.ecommerce.product.dto.request.BrandCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.BrandUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.BrandResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin Brand Management Controller
 * ROLE_ADMIN only - Create, Edit, Delete brands
 */
@Slf4j
@Controller
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBrandController {

    private final BrandService brandService;

    @GetMapping
    public String listBrands(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            Model model) {
        try {
            List<BrandResponseDto> brands;
            
            // Apply search, filter, and sort
            if ((search != null && !search.isEmpty()) || (filter != null && !filter.isEmpty())) {
                brands = brandService.searchFilterSort(search, filter, sortBy, order);
            } else {
                brands = brandService.getAllBrandsSorted(sortBy, order);
            }
            
            model.addAttribute("title", "Brand Management");
            model.addAttribute("brands", brands);
            model.addAttribute("hasBrands", !brands.isEmpty());
            model.addAttribute("search", search);
            model.addAttribute("filter", filter);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("order", order);
            return "admin/brands/list";
        } catch (Exception e) {
            log.error("Error loading brands", e);
            model.addAttribute("errorMessage", "Unable to load brands");
            return "admin/brands/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("title", "Create Brand");
        model.addAttribute("brand", new BrandCreateRequestDto());
        return "admin/brands/form";
    }

    @PostMapping
    public String createBrand(@ModelAttribute BrandCreateRequestDto requestDto,
                             RedirectAttributes redirectAttributes) {
        try {
            brandService.createBrand(requestDto);
            redirectAttributes.addFlashAttribute("success", "Brand created successfully");
            return "redirect:/admin/brands";
        } catch (Exception e) {
            log.error("Error creating brand", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create brand: " + e.getMessage());
            return "redirect:/admin/brands/new";
        }
    }

    @GetMapping("/{brandId}/edit")
    public String showEditForm(@PathVariable Long brandId, Model model) {
        try {
            BrandResponseDto brand = brandService.getBrandById(brandId);
            BrandUpdateRequestDto updateDto = new BrandUpdateRequestDto();
            updateDto.setName(brand.getName());
            updateDto.setDescription(brand.getDescription());
            model.addAttribute("title", "Edit Brand");
            model.addAttribute("brand", updateDto);
            model.addAttribute("brandId", brandId);
            return "admin/brands/form";
        } catch (Exception e) {
            log.error("Error loading brand for edit", e);
            return "redirect:/admin/brands";
        }
    }

    @PostMapping("/{brandId}")
    public String updateBrand(@PathVariable Long brandId,
                             @ModelAttribute BrandUpdateRequestDto requestDto,
                             RedirectAttributes redirectAttributes) {
        try {
            brandService.updateBrand(brandId, requestDto);
            redirectAttributes.addFlashAttribute("success", "Brand updated successfully");
            return "redirect:/admin/brands";
        } catch (Exception e) {
            log.error("Error updating brand", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update brand: " + e.getMessage());
            return "redirect:/admin/brands/" + brandId + "/edit";
        }
    }

    @PostMapping("/{brandId}/delete")
    public String deleteBrand(@PathVariable Long brandId,
                             RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(brandId);
            redirectAttributes.addFlashAttribute("success", "Brand deleted successfully");
            return "redirect:/admin/brands";
        } catch (Exception e) {
            log.error("Error deleting brand", e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete brand: " + e.getMessage());
            return "redirect:/admin/brands";
        }
    }

    @PostMapping("/{brandId}/activate")
    public String activateBrand(@PathVariable Long brandId,
                               RedirectAttributes redirectAttributes) {
        try {
            brandService.activateBrand(brandId);
            redirectAttributes.addFlashAttribute("success", "Brand activated successfully");
            return "redirect:/admin/brands";
        } catch (Exception e) {
            log.error("Error activating brand", e);
            redirectAttributes.addFlashAttribute("error", "Failed to activate brand: " + e.getMessage());
            return "redirect:/admin/brands";
        }
    }

    @PostMapping("/{brandId}/deactivate")
    public String deactivateBrand(@PathVariable Long brandId,
                                 RedirectAttributes redirectAttributes) {
        try {
            brandService.deactivateBrand(brandId);
            redirectAttributes.addFlashAttribute("success", "Brand deactivated successfully");
            return "redirect:/admin/brands";
        } catch (Exception e) {
            log.error("Error deactivating brand", e);
            redirectAttributes.addFlashAttribute("error", "Failed to deactivate brand: " + e.getMessage());
            return "redirect:/admin/brands";
        }
    }
}

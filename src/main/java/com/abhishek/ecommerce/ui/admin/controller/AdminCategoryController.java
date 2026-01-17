package com.abhishek.ecommerce.ui.admin.controller;

import com.abhishek.ecommerce.product.service.CategoryService;
import com.abhishek.ecommerce.product.dto.request.CategoryCreateRequestDto;
import com.abhishek.ecommerce.product.dto.request.CategoryUpdateRequestDto;
import com.abhishek.ecommerce.product.dto.response.CategoryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Admin Category Management Controller
 * ROLE_ADMIN only - Create, Edit, Delete categories
 */
@Slf4j
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        try {
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
            model.addAttribute("title", "Category Management");
            model.addAttribute("categories", categories);
            model.addAttribute("hasCategories", !categories.isEmpty());
            return "admin/categories/list";
        } catch (Exception e) {
            log.error("Error loading categories", e);
            model.addAttribute("errorMessage", "Unable to load categories");
            return "admin/categories/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("title", "Create Category");
        model.addAttribute("category", new CategoryCreateRequestDto());
        return "admin/categories/form";
    }

    @PostMapping
    public String createCategory(@ModelAttribute CategoryCreateRequestDto requestDto,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(requestDto);
            redirectAttributes.addFlashAttribute("success", "Category created successfully");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            log.error("Error creating category", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create category: " + e.getMessage());
            return "redirect:/admin/categories/new";
        }
    }

    @GetMapping("/{categoryId}/edit")
    public String showEditForm(@PathVariable Long categoryId, Model model) {
        try {
            CategoryResponseDto category = categoryService.getCategoryById(categoryId);
            CategoryUpdateRequestDto updateDto = new CategoryUpdateRequestDto();
            updateDto.setName(category.getName());
            updateDto.setDescription(category.getDescription());
            model.addAttribute("title", "Edit Category");
            model.addAttribute("category", updateDto);
            model.addAttribute("categoryId", categoryId);
            return "admin/categories/form";
        } catch (Exception e) {
            log.error("Error loading category for edit", e);
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/{categoryId}")
    public String updateCategory(@PathVariable Long categoryId,
                                 @ModelAttribute CategoryUpdateRequestDto requestDto,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(categoryId, requestDto);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            log.error("Error updating category", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update category: " + e.getMessage());
            return "redirect:/admin/categories/" + categoryId + "/edit";
        }
    }

    @PostMapping("/{categoryId}/delete")
    public String deleteCategory(@PathVariable Long categoryId,
                                  RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            log.error("Error deleting category", e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete category: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }
}

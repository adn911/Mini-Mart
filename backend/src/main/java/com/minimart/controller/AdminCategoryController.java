package com.minimart.controller;

import com.minimart.entity.Category;
import com.minimart.service.CategoryService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/admin/categories")
    List<Category> listCategories(
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return categoryService.getAllCategories(includeDeleted);
    }

    @PostMapping("/api/admin/categories")
    ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/admin/categories/{id}")
    ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.updateCategory(id, category);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/admin/categories/{id}")
    ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (categoryService.softDeleteCategory(id)) {
            return ResponseEntity.ok(Map.of("status", "deleted"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
    }
}

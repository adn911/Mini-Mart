package com.minimart.controller;

import com.minimart.entity.Category;
import com.minimart.entity.Product;
import com.minimart.service.ProductService;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PublicCatalogController {

    private final ProductService productService;

    public PublicCatalogController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/products")
    ResponseEntity<?> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<Product> result = productService.getActiveProducts(search, categoryId, page, size);
        return ResponseEntity.ok(Map.of(
            "content", result.getContent(),
            "page", result.getNumber(),
            "size", result.getSize(),
            "totalPages", result.getTotalPages(),
            "totalElements", result.getTotalElements()
        ));
    }

    @GetMapping("/api/products/{id}")
    ResponseEntity<?> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null || product.getStatus() != com.minimart.entity.EntityStatus.ACTIVE) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/api/categories")
    List<Category> getCategories() {
        return productService.getActiveCategories();
    }
}

package com.minimart.controller;

import com.minimart.entity.Product;
import com.minimart.service.ProductService;
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
class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/admin/products")
    List<Product> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return productService.getProductsForAdmin(search, categoryId, includeDeleted);
    }

    @GetMapping("/api/admin/products/{id}")
    ResponseEntity<?> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping("/api/admin/products")
    ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/admin/products/{id}")
    ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updated = productService.updateProduct(id, product);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/admin/products/{id}")
    ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (productService.softDeleteProduct(id)) {
            return ResponseEntity.ok(Map.of("status", "deleted"));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/api/admin/products/{id}/refill")
    ResponseEntity<?> refillProduct(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int quantity = body.getOrDefault("quantity", 0);
        if (quantity <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be positive"));
        }
        if (productService.refillProduct(id, quantity)) {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }
}

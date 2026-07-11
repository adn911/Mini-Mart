package com.minimart.controller;

import com.minimart.entity.Product;
import com.minimart.service.ProductService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
class AdminProductController {

    private final ProductService productService;

    @Value("${app.upload.path:uploads/products}")
    private String uploadPath;

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/admin/products/{id}")
    ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (productService.softDeleteProduct(id)) {
            return ResponseEntity.ok(Map.of("status", "deleted"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
    }

    @PostMapping("/api/admin/products/{id}/image")
    ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !Set.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unsupported image type. Accepted: JPEG, PNG, WebP"));
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image must be 5 MB or smaller"));
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
        }

        String ext = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
        String filename = "product-" + id + "-" + System.currentTimeMillis() + ext;

        try {
            Files.createDirectories(Path.of(uploadPath));
            Files.copy(file.getInputStream(), Path.of(uploadPath, filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save image"));
        }

        String imageUrl = "/uploads/" + filename;
        productService.updateProductImageUrl(id, imageUrl);

        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}

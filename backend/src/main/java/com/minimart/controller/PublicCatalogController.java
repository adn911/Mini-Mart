package com.minimart.controller;

import com.minimart.entity.Category;
import com.minimart.entity.Product;
import com.minimart.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PublicCatalogController {

    private final ProductService productService;

    public PublicCatalogController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/products")
    List<Product> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId) {
        return productService.getActiveProducts(search, categoryId);
    }

    @GetMapping("/api/categories")
    List<Category> getCategories() {
        return productService.getActiveCategories();
    }
}

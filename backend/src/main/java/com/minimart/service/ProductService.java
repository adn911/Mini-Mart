package com.minimart.service;

import com.minimart.entity.Category;
import com.minimart.entity.EntityStatus;
import com.minimart.entity.Product;
import com.minimart.repository.CategoryRepository;
import com.minimart.repository.ProductRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<Product> getActiveProducts(String search, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findActiveProducts(
            search != null && !search.isBlank() ? search : null,
            categoryId,
            pageable
        );
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatusOrderByIdAsc(EntityStatus.ACTIVE);
    }

    public List<Product> getProductsForAdmin(String search, Long categoryId, boolean includeDeleted) {
        return productRepository.findProductsForAdmin(
            search != null && !search.isBlank() ? search : null,
            categoryId,
            includeDeleted
        );
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Transactional
    public Product createProduct(Product product) {
        product.setId(null);
        product.setStatus(EntityStatus.ACTIVE);
        product.setReservedQuantity(0);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product update) {
        Product existing = productRepository.findById(id).orElse(null);
        if (existing == null) return null;

        if (update.getName() != null) existing.setName(update.getName());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getPrice() != null) existing.setPrice(update.getPrice());
        if (update.getCategory() != null && update.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(update.getCategory().getId()).orElse(null);
            existing.setCategory(cat);
        }
        if (update.getImageUrl() != null) existing.setImageUrl(update.getImageUrl());
        if (update.getStockQuantity() >= 0) existing.setStockQuantity(update.getStockQuantity());

        return productRepository.save(existing);
    }

    @Transactional
    public boolean softDeleteProduct(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return false;
        product.setStatus(EntityStatus.DELETED);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public boolean refillProduct(Long id, int quantity) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return false;
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
        return true;
    }
}

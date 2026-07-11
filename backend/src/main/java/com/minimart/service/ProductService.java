package com.minimart.service;

import com.minimart.entity.Category;
import com.minimart.entity.EntityStatus;
import com.minimart.entity.Product;
import com.minimart.repository.CategoryRepository;
import com.minimart.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getActiveProducts(String search, Long categoryId) {
        return productRepository.findActiveProducts(
            search != null && !search.isBlank() ? search : null,
            categoryId
        );
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatusOrderByIdAsc(EntityStatus.ACTIVE);
    }
}

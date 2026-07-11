package com.minimart.service;

import com.minimart.entity.Category;
import com.minimart.entity.EntityStatus;
import com.minimart.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatusOrderByIdAsc(EntityStatus.ACTIVE);
    }

    public List<Category> getAllCategories(boolean includeDeleted) {
        if (includeDeleted) {
            return categoryRepository.findAll();
        }
        return getActiveCategories();
    }

    @Transactional
    public Category createCategory(Category category) {
        category.setId(null);
        category.setStatus(EntityStatus.ACTIVE);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category update) {
        Category existing = categoryRepository.findById(id).orElse(null);
        if (existing == null) return null;
        if (update.getName() != null) existing.setName(update.getName());
        return categoryRepository.save(existing);
    }

    @Transactional
    public boolean softDeleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) return false;
        category.setStatus(EntityStatus.DELETED);
        categoryRepository.save(category);
        return true;
    }
}

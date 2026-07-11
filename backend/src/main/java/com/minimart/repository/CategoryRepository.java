package com.minimart.repository;

import com.minimart.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByStatusOrderByIdAsc(com.minimart.entity.EntityStatus status);
}

package com.minimart.repository;

import com.minimart.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatus(com.minimart.entity.EntityStatus status);

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = 'ACTIVE'
        AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        ORDER BY p.id ASC
    """)
    List<Product> findActiveProducts(@Param("search") String search,
                                     @Param("categoryId") Long categoryId);
}

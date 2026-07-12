package com.minimart.repository;

import com.minimart.entity.CustomerOrder;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT o FROM CustomerOrder o WHERE " +
           "(:search IS NULL OR LOWER(o.sessionId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:name IS NULL OR LOWER(o.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(o.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:city IS NULL OR LOWER(o.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:dateFrom IS NULL OR o.createdAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR o.createdAt <= :dateTo) " +
           "ORDER BY o.createdAt DESC")
    List<CustomerOrder> searchOrders(@Param("search") String search, @Param("name") String name,
                                      @Param("city") String city, @Param("status") String status,
                                      @Param("dateFrom") Instant dateFrom, @Param("dateTo") Instant dateTo);
}

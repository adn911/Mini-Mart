package com.minimart.repository;

import com.minimart.entity.CustomerOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findBySessionIdContainingIgnoreCase(String sessionId);

    List<CustomerOrder> findByStatus(String status);

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT o FROM CustomerOrder o WHERE " +
           "(:search IS NULL OR LOWER(o.sessionId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    List<CustomerOrder> searchOrders(@Param("search") String search, @Param("status") String status);
}

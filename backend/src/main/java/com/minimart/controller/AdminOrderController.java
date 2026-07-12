package com.minimart.controller;

import com.minimart.entity.CustomerOrder;
import com.minimart.repository.CustomerOrderRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AdminOrderController {

    private final CustomerOrderRepository customerOrderRepository;

    public AdminOrderController(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    @GetMapping("/api/admin/orders")
    List<CustomerOrder> listOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        if (search != null && !search.isBlank()) {
            return customerOrderRepository.searchOrders(search, status);
        }
        if (status != null && !status.isBlank()) {
            return customerOrderRepository.findByStatus(status);
        }
        return customerOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    @PutMapping("/api/admin/orders/{id}/status")
    ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        CustomerOrder order = customerOrderRepository.findById(id).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }
        order.setStatus(newStatus);
        customerOrderRepository.save(order);
        return ResponseEntity.ok(order);
    }
}
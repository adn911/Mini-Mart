package com.minimart.controller;

import com.minimart.entity.CustomerOrder;
import com.minimart.repository.CustomerOrderRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
}
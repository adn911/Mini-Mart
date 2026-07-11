package com.minimart.repository;

import com.minimart.entity.Cart;
import com.minimart.entity.CartItem;
import com.minimart.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}

package com.minimart.repository;

import com.minimart.entity.Cart;
import com.minimart.entity.CartStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findBySessionIdAndStatus(String sessionId, CartStatus status);
}

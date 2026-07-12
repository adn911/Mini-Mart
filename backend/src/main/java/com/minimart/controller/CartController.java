package com.minimart.controller;

import com.minimart.entity.CartItem;
import com.minimart.entity.CustomerOrder;
import com.minimart.service.CartService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/api/cart")
    ResponseEntity<?> getCart(HttpSession session) {
        String sessionId = session.getId();
        var result = cartService.getCartItems(sessionId);

        int itemCount = result.items().stream().mapToInt(i -> i.getQuantity()).sum();
        return ResponseEntity.ok(Map.of(
            "items", result.items(),
            "itemCount", itemCount,
            "expired", result.expired()
        ));
    }

    @PostMapping("/api/cart/items")
    ResponseEntity<?> addItem(HttpSession session, @RequestBody Map<String, Object> body) {
        try {
            Long productId = Long.valueOf(body.get("productId").toString());
            int quantity = Integer.parseInt(body.getOrDefault("quantity", "1").toString());
            CartItem item = cartService.addItem(session.getId(), productId, quantity);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/cart/items/{itemId}")
    ResponseEntity<?> updateItem(HttpSession session, @PathVariable Long itemId,
                                  @RequestBody Map<String, Object> body) {
        try {
            int quantity = Integer.parseInt(body.get("quantity").toString());
            CartItem item = cartService.updateItemQuantity(session.getId(), itemId, quantity);
            if (item == null) {
                return ResponseEntity.ok(Map.of("status", "removed"));
            }
            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/cart/items/{itemId}")
    ResponseEntity<?> removeItem(HttpSession session, @PathVariable Long itemId) {
        cartService.removeItem(session.getId(), itemId);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    @PostMapping("/api/cart/checkout")
    ResponseEntity<?> checkout(HttpSession session, @RequestBody(required = false) Map<String, Object> body) {
        try {
            String paymentMethod = body != null ? (String) body.get("paymentMethod") : null;
            CartService.ShippingAddress shipping = null;
            if (body != null) {
                shipping = new CartService.ShippingAddress(
                    (String) body.get("firstName"),
                    (String) body.get("lastName"),
                    (String) body.get("addressLine"),
                    (String) body.get("city"),
                    (String) body.get("zipCode"),
                    (String) body.get("phone1"),
                    (String) body.get("phone2")
                );
            }
            CustomerOrder order = cartService.checkout(session.getId(), paymentMethod, shipping);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

package com.minimart.service;

import com.minimart.entity.Cart;
import com.minimart.entity.CartItem;
import com.minimart.entity.CartStatus;
import com.minimart.entity.Product;
import com.minimart.repository.CartItemRepository;
import com.minimart.repository.CartRepository;
import com.minimart.repository.ProductRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public Cart getOrCreateCart(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
            .orElseGet(() -> {
                Cart cart = new Cart(sessionId);
                return cartRepository.save(cart);
            });
    }

    public Cart getCart(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE).orElse(null);
    }

    public List<CartItem> getCartItems(String sessionId) {
        Cart cart = getCart(sessionId);
        if (cart == null) return List.of();
        cart.setLastAccessedAt(Instant.now());
        cartRepository.save(cart);
        return cartItemRepository.findAll().stream()
            .filter(item -> item.getCart().getId().equals(cart.getId()))
            .toList();
    }

    @Transactional
    public CartItem addItem(String sessionId, Long productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) throw new IllegalArgumentException("Product not found");

        int available = product.getStockQuantity() - product.getReservedQuantity();
        if (quantity > available) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        Cart cart = getOrCreateCart(sessionId);
        cart.setLastAccessedAt(Instant.now());
        cartRepository.save(cart);

        CartItem existing = cartItemRepository.findByCartAndProduct(cart, product).orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartItemRepository.save(existing);
        } else {
            existing = cartItemRepository.save(new CartItem(cart, product, quantity));
        }

        product.setReservedQuantity(product.getReservedQuantity() + quantity);
        productRepository.save(product);

        return existing;
    }

    @Transactional
    public CartItem updateItemQuantity(String sessionId, Long itemId, int newQuantity) {
        if (newQuantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");

        Cart cart = getCart(sessionId);
        if (cart == null) throw new IllegalStateException("Cart not found");

        CartItem item = cartItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item not found");
        }

        Product product = item.getProduct();
        int quantityDiff = newQuantity - item.getQuantity();

        if (quantityDiff > 0) {
            int available = product.getStockQuantity() - product.getReservedQuantity();
            if (quantityDiff > available) {
                throw new IllegalArgumentException("Insufficient stock");
            }
            product.setReservedQuantity(product.getReservedQuantity() + quantityDiff);
        } else {
            product.setReservedQuantity(product.getReservedQuantity() + quantityDiff);
        }

        if (newQuantity == 0) {
            cartItemRepository.delete(item);
            productRepository.save(product);
            return null;
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        productRepository.save(product);

        cart.setLastAccessedAt(Instant.now());
        cartRepository.save(cart);

        return item;
    }

    @Transactional
    public void removeItem(String sessionId, Long itemId) {
        Cart cart = getCart(sessionId);
        if (cart == null) return;

        CartItem item = cartItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getCart().getId().equals(cart.getId())) return;

        Product product = item.getProduct();
        product.setReservedQuantity(product.getReservedQuantity() - item.getQuantity());
        productRepository.save(product);

        cartItemRepository.delete(item);

        cart.setLastAccessedAt(Instant.now());
        cartRepository.save(cart);
    }
}

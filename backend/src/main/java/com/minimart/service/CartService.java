package com.minimart.service;

import com.minimart.entity.Cart;
import com.minimart.entity.CartItem;
import com.minimart.entity.CartStatus;
import com.minimart.entity.CustomerOrder;
import com.minimart.entity.OrderItem;
import com.minimart.entity.Product;
import com.minimart.repository.CartItemRepository;
import com.minimart.repository.CartRepository;
import com.minimart.repository.CustomerOrderRepository;
import com.minimart.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository customerOrderRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       CustomerOrderRepository customerOrderRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.customerOrderRepository = customerOrderRepository;
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

    public record CartItemsResult(List<CartItem> items, boolean expired) {}

    public CartItemsResult getCartItems(String sessionId) {
        Cart cart = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
            .orElse(cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.EXPIRED).orElse(null));
        if (cart == null) return new CartItemsResult(List.of(), false);
        if (cart.getStatus() == CartStatus.ACTIVE) {
            cart.setLastAccessedAt(Instant.now());
            cartRepository.save(cart);
            return new CartItemsResult(cartItemRepository.findByCart(cart), false);
        }
        return new CartItemsResult(cartItemRepository.findByCart(cart), true);
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

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void expireStaleCarts() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(30));
        List<Cart> staleCarts = cartRepository.findByStatusAndLastAccessedAtBefore(CartStatus.ACTIVE, cutoff);
        for (Cart cart : staleCarts) {
            List<CartItem> items = cartItemRepository.findByCart(cart);
            for (CartItem item : items) {
                Product product = item.getProduct();
                product.setReservedQuantity(product.getReservedQuantity() - item.getQuantity());
                productRepository.save(product);
            }
            cart.setStatus(CartStatus.EXPIRED);
            cartRepository.save(cart);
        }
    }

    public record ShippingAddress(
        String firstName, String lastName, String addressLine,
        String city, String zipCode, String phone1, String phone2
    ) {}

    @Transactional
    public CustomerOrder checkout(String sessionId, String paymentMethod, ShippingAddress shipping) {
        Cart cart = cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE).orElse(null);
        if (cart == null) {
            if (cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.EXPIRED).isPresent()) {
                throw new IllegalStateException("Cart has expired. Items have been returned to stock.");
            }
            throw new IllegalStateException("Cart is empty");
        }

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) throw new IllegalStateException("Cart is empty");

        CustomerOrder order = new CustomerOrder();
        order.setSessionId(sessionId);
        if (paymentMethod != null) order.setPaymentMethod(paymentMethod);
        if (shipping != null) {
            order.setFirstName(shipping.firstName());
            order.setLastName(shipping.lastName());
            order.setAddressLine(shipping.addressLine());
            order.setCity(shipping.city());
            order.setZipCode(shipping.zipCode());
            order.setPhone1(shipping.phone1());
            order.setPhone2(shipping.phone2());
        }

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getReservedQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient reserved stock for " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            product.setReservedQuantity(product.getReservedQuantity() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(order, product, item.getQuantity(), product.getPrice());
            order.getItems().add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        order.setTotal(total);

        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        return customerOrderRepository.save(order);
    }
}

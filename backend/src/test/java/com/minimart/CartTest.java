package com.minimart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimart.entity.Cart;
import com.minimart.entity.CartStatus;
import com.minimart.entity.Category;
import com.minimart.entity.Product;
import com.minimart.repository.CartItemRepository;
import com.minimart.repository.CartRepository;
import com.minimart.repository.CategoryRepository;
import com.minimart.repository.CustomerOrderRepository;
import com.minimart.repository.ProductRepository;
import com.minimart.service.CartService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CartTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private CartService cartService;

    private MockHttpSession session;
    private Product product;

    @BeforeEach
    void setUp() {
        customerOrderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        session = new MockHttpSession();

        Category cat = categoryRepository.save(new Category("Test"));

        product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(10);
        product.setCategory(cat);
        product = productRepository.save(product);
    }

    @Test
    void emptyCartReturnsZeroCount() throws Exception {
        mockMvc.perform(get("/api/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void addItemToCart() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.product.id").value(product.getId()));
    }

    @Test
    void cartReflectsItemCount() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 3))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itemCount").value(3));
    }

    @Test
    void addItemFailsWhenStockInsufficient() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 20))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Insufficient stock"));
    }

    @Test
    void addMultipleReservesStock() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 4))))
            .andExpect(status().isCreated());

        // Add more of same product
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 3))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.quantity").value(7));
    }

    @Test
    void updateItemQuantity() throws Exception {
        String response = mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
            .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/cart/items/" + itemId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("quantity", 5))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void removeItemReleasesStock() throws Exception {
        // Add to cart
        String response = mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 3))))
            .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(response).get("id").asLong();

        // Product should have reservedQuantity = 3
        mockMvc.perform(get("/api/products/" + product.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservedQuantity").value(3));

        // Remove from cart
        mockMvc.perform(delete("/api/cart/items/" + itemId)
                .session(session))
            .andExpect(status().isOk());

        // Cart should be empty
        mockMvc.perform(get("/api/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void sessionsAreIsolated() throws Exception {
        MockHttpSession session2 = new MockHttpSession();

        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cart").session(session))
            .andExpect(jsonPath("$.itemCount").value(2));

        mockMvc.perform(get("/api/cart").session(session2))
            .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void checkoutFailsWhenCartIsEmpty() throws Exception {
        mockMvc.perform(post("/api/cart/checkout")
                .session(session))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Cart is empty"));
    }

    @Test
    void successfulCheckoutCreatesOrderAndDeductsStock() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 3))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cart/checkout")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PLACED"))
            .andExpect(jsonPath("$.total").value(29.97))
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].quantity").value(3))
            .andExpect(jsonPath("$.items[0].unitPrice").value(9.99))
            .andExpect(jsonPath("$.items[0].product.id").value(product.getId()));

        // Stock should be deducted
        mockMvc.perform(get("/api/products/" + product.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(7))
            .andExpect(jsonPath("$.reservedQuantity").value(0));

        // Cart should be empty
        mockMvc.perform(get("/api/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void checkoutFailsAfterCartAlreadyCheckedOut() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cart/checkout")
                .session(session))
            .andExpect(status().isOk());

        // Second checkout should fail
        mockMvc.perform(post("/api/cart/checkout")
                .session(session))
            .andExpect(status().isBadRequest());
    }

    @Test
    void checkoutWithMultipleItems() throws Exception {
        Product product2 = new Product();
        product2.setName("Second Product");
        product2.setPrice(new BigDecimal("4.49"));
        product2.setStockQuantity(20);
        product2.setCategory(product.getCategory());
        product2 = productRepository.save(product2);

        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product2.getId(), "quantity", 4))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/cart/checkout")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.total").value(9.99 * 2 + 4.49 * 4));
    }

    @Test
    void expireStaleCartsReleasesReservedStock() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 5))))
            .andExpect(status().isCreated());

        Cart cart = cartRepository.findBySessionIdAndStatus(session.getId(), CartStatus.ACTIVE).orElseThrow();
        cart.setLastAccessedAt(Instant.now().minus(java.time.Duration.ofMinutes(31)));
        cartRepository.save(cart);

        cartService.expireStaleCarts();

        mockMvc.perform(get("/api/products/" + product.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reservedQuantity").value(0))
            .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    void expiredCartItemsAreReturned() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 3))))
            .andExpect(status().isCreated());

        Cart cart = cartRepository.findBySessionIdAndStatus(session.getId(), CartStatus.ACTIVE).orElseThrow();
        cart.setLastAccessedAt(Instant.now().minus(java.time.Duration.ofMinutes(31)));
        cartRepository.save(cart);

        cartService.expireStaleCarts();

        mockMvc.perform(get("/api/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.expired").value(true))
            .andExpect(jsonPath("$.itemCount").value(3));
    }

    @Test
    void checkoutFailsForExpiredCart() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
            .andExpect(status().isCreated());

        Cart cart = cartRepository.findBySessionIdAndStatus(session.getId(), CartStatus.ACTIVE).orElseThrow();
        cart.setLastAccessedAt(Instant.now().minus(java.time.Duration.ofMinutes(31)));
        cartRepository.save(cart);

        cartService.expireStaleCarts();

        mockMvc.perform(post("/api/cart/checkout")
                .session(session))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Cart has expired. Items have been returned to stock."));
    }

    @Test
    void expiredCartResetsWhenUserAddsNewItem() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 2))))
            .andExpect(status().isCreated());

        Cart cart = cartRepository.findBySessionIdAndStatus(session.getId(), CartStatus.ACTIVE).orElseThrow();
        cart.setLastAccessedAt(Instant.now().minus(java.time.Duration.ofMinutes(31)));
        cartRepository.save(cart);

        cartService.expireStaleCarts();

        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", product.getId(), "quantity", 1))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.expired").value(false))
            .andExpect(jsonPath("$.itemCount").value(1));
    }
}

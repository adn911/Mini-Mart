package com.minimart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimart.entity.Category;
import com.minimart.entity.Product;
import com.minimart.repository.CartItemRepository;
import com.minimart.repository.CartRepository;
import com.minimart.repository.CategoryRepository;
import com.minimart.repository.ProductRepository;
import java.math.BigDecimal;
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

    private MockHttpSession session;
    private Product product;

    @BeforeEach
    void setUp() {
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
}

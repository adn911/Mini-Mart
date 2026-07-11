package com.minimart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimart.entity.EntityStatus;
import com.minimart.repository.CartItemRepository;
import com.minimart.repository.CartRepository;
import com.minimart.repository.CategoryRepository;
import com.minimart.repository.CustomerOrderRepository;
import com.minimart.repository.ProductRepository;
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
class EndToEndTest {

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

    private MockHttpSession session;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        customerOrderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        session = new MockHttpSession();

        String response = mockMvc.perform(post("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", "admin", "password", "admin123"
                ))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        adminToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void fullShopperFlow() throws Exception {
        // Admin sets up categories and products first
        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Drinks"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());

        String catResponse = mockMvc.perform(get("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Long categoryId = objectMapper.readTree(catResponse).get(0).get("id").asLong();

        mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Sparkling Water",
                    "price", 1.49,
                    "stockQuantity", 100,
                    "category", Map.of("id", categoryId)
                ))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Cola",
                    "price", 1.99,
                    "stockQuantity", 50,
                    "category", Map.of("id", categoryId)
                ))))
            .andExpect(status().isCreated());

        // Shopper browses catalog
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.page").value(0));

        // Shopper searches
        mockMvc.perform(get("/api/products?search=Cola"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Cola"));

        // Shopper filters by category
        mockMvc.perform(get("/api/products?categoryId=" + categoryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));

        // Shopper views categories
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        // Shopper adds to cart
        String productsJson = mockMvc.perform(get("/api/products?search=Cola"))
            .andReturn().getResponse().getContentAsString();
        Long colaId = objectMapper.readTree(productsJson).get("content").get(0).get("id").asLong();

        String addItemJson = mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", colaId, "quantity", 3))))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long itemId = objectMapper.readTree(addItemJson).get("id").asLong();

        // Shopper views cart
        mockMvc.perform(get("/api/cart").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itemCount").value(3))
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.expired").value(false));

        // Shopper updates quantity
        mockMvc.perform(put("/api/cart/items/" + itemId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("quantity", 5))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(5));

        // Shopper checks out
        mockMvc.perform(post("/api/cart/checkout")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PLACED"))
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].quantity").value(5))
            .andExpect(jsonPath("$.total").value(9.95));

        // Stock was deducted
        mockMvc.perform(get("/api/products/" + colaId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(45))
            .andExpect(jsonPath("$.reservedQuantity").value(0));
    }

    @Test
    void fullAdminFlow() throws Exception {
        // Create category
        String catJson = mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Snacks"))))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long catId = objectMapper.readTree(catJson).get("id").asLong();

        // Create product
        String prodJson = mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Potato Chips",
                    "price", 2.49,
                    "stockQuantity", 30,
                    "category", Map.of("id", catId)
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Potato Chips"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn().getResponse().getContentAsString();

        Long prodId = objectMapper.readTree(prodJson).get("id").asLong();

        // Update product
        mockMvc.perform(put("/api/admin/products/" + prodId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Potato Chips Salted", "price", 2.99))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Potato Chips Salted"))
            .andExpect(jsonPath("$.price").value(2.99));

        // Public sees updated product, not deleted
        mockMvc.perform(get("/api/products/" + prodId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Potato Chips Salted"));

        // Refill stock
        mockMvc.perform(post("/api/admin/products/" + prodId + "/refill")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("quantity", 20))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(50));

        // Soft delete
        mockMvc.perform(delete("/api/admin/products/" + prodId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("deleted"));

        // Public no longer sees it
        mockMvc.perform(get("/api/products/" + prodId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));

        // Admin can see it with includeDeleted
        mockMvc.perform(get("/api/admin/products?includeDeleted=true")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        // Admin can see it without includeDeleted
        mockMvc.perform(get("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        // Delete category
        mockMvc.perform(delete("/api/admin/categories/" + catId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("deleted"));

        // Public categories hide deleted
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        // Admin can see deleted categories
        mockMvc.perform(get("/api/admin/categories?includeDeleted=true")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void nonExistentResourceReturnsConsistentError() throws Exception {
        mockMvc.perform(get("/api/products/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));

        mockMvc.perform(get("/api/admin/products/99999")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));

        mockMvc.perform(put("/api/admin/products/99999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Nope"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));

        mockMvc.perform(delete("/api/admin/products/99999")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));

        mockMvc.perform(put("/api/admin/categories/99999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Nope"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));

        mockMvc.perform(delete("/api/admin/categories/99999")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));
    }

    @Test
    void inventoryMetricsConsistent() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Drinks"))));

        String catList = mockMvc.perform(get("/api/admin/categories")
                .header("Authorization", "Bearer " + adminToken)).andReturn().getResponse().getContentAsString();
        Long catId = objectMapper.readTree(catList).get(0).get("id").asLong();

        mockMvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Water",
                    "price", 0.99,
                    "stockQuantity", 20,
                    "category", Map.of("id", catId)
                ))));

        Long prodId = objectMapper.readTree(
            mockMvc.perform(get("/api/admin/products")
                    .header("Authorization", "Bearer " + adminToken))
                .andReturn().getResponse().getContentAsString()
        ).get(0).get("id").asLong();

        // stock=20, reserved=0, available=20
        mockMvc.perform(get("/api/products/" + prodId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(20))
            .andExpect(jsonPath("$.reservedQuantity").value(0))
            .andExpect(jsonPath("$.availableQuantity").value(20));

        // Reserve 7 via cart
        mockMvc.perform(post("/api/cart/items")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("productId", prodId, "quantity", 7))))
            .andExpect(status().isCreated());

        // stock=20, reserved=7, available=13
        mockMvc.perform(get("/api/products/" + prodId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(20))
            .andExpect(jsonPath("$.reservedQuantity").value(7))
            .andExpect(jsonPath("$.availableQuantity").value(13));

        // Checkout reduces stock and clears reservation
        mockMvc.perform(post("/api/cart/checkout")
                .session(session))
            .andExpect(status().isOk());

        // stock=13, reserved=0, available=13
        mockMvc.perform(get("/api/products/" + prodId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(13))
            .andExpect(jsonPath("$.reservedQuantity").value(0))
            .andExpect(jsonPath("$.availableQuantity").value(13));

        // Refill adds to stock
        mockMvc.perform(post("/api/admin/products/" + prodId + "/refill")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("quantity", 10))))
            .andExpect(status().isOk());

        // stock=23, reserved=0, available=23
        mockMvc.perform(get("/api/products/" + prodId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(23))
            .andExpect(jsonPath("$.reservedQuantity").value(0))
            .andExpect(jsonPath("$.availableQuantity").value(23));
    }
}

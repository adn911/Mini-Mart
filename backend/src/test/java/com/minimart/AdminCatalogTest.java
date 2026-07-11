package com.minimart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimart.entity.Category;
import com.minimart.entity.EntityStatus;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCatalogTest {

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

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

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

    private String auth() {
        return "Bearer " + adminToken;
    }

    @Test
    void createAndListProducts() throws Exception {
        Category cat = categoryRepository.save(new Category("Drinks"));

        mockMvc.perform(post("/api/admin/products")
                .header("Authorization", auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Cola",
                    "price", 1.99,
                    "stockQuantity", 100,
                    "category", Map.of("id", cat.getId())
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Cola"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/admin/products")
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateProduct() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        productRepository.save(p);

        mockMvc.perform(put("/api/admin/products/" + p.getId())
                .header("Authorization", auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Cola Zero", "price", 2.49))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Cola Zero"))
            .andExpect(jsonPath("$.price").value(2.49));
    }

    @Test
    void softDeleteProduct() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        productRepository.save(p);

        mockMvc.perform(delete("/api/admin/products/" + p.getId())
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("deleted"));

        mockMvc.perform(get("/api/admin/products")
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/admin/products?includeDeleted=true")
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void refillProduct() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(50);
        productRepository.save(p);

        mockMvc.perform(post("/api/admin/products/" + p.getId() + "/refill")
                .header("Authorization", auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("quantity", 30))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(80));
    }

    @Test
    void createAndListCategories() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                .header("Authorization", auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Drinks"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Drinks"));

        mockMvc.perform(get("/api/admin/categories")
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void softDeleteCategory() throws Exception {
        Category cat = categoryRepository.save(new Category("Drinks"));

        mockMvc.perform(delete("/api/admin/categories/" + cat.getId())
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("deleted"));

        mockMvc.perform(get("/api/admin/categories")
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/admin/categories?includeDeleted=true")
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void publicApiStillHidesDeleted() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        p.setStatus(EntityStatus.DELETED);
        productRepository.save(p);

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/admin/products"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }
}

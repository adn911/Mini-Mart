package com.minimart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.minimart.entity.Category;
import com.minimart.entity.EntityStatus;
import com.minimart.entity.Product;
import com.minimart.repository.CartItemRepository;
import com.minimart.repository.CartRepository;
import com.minimart.repository.CategoryRepository;
import com.minimart.repository.CustomerOrderRepository;
import com.minimart.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PublicCatalogTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        customerOrderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void returnsActiveCategories() throws Exception {
        categoryRepository.save(new Category("Drinks"));

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Drinks"));
    }

    @Test
    void hidesDeletedCategories() throws Exception {
        Category cat = categoryRepository.save(new Category("Drinks"));
        cat.setStatus(EntityStatus.DELETED);
        categoryRepository.save(cat);

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsActiveProducts() throws Exception {
        Category cat = categoryRepository.save(new Category("Drinks"));
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(50);
        p.setCategory(cat);
        productRepository.save(p);

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Cola"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void hidesDeletedProducts() throws Exception {
        Category cat = categoryRepository.save(new Category("Drinks"));
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(50);
        p.setCategory(cat);
        p.setStatus(EntityStatus.DELETED);
        productRepository.save(p);

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void filtersProductsBySearch() throws Exception {
        Category cat = categoryRepository.save(new Category("Drinks"));
        Product p = new Product();
        p.setName("Cola");
        p.setDescription("A fizzy drink");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(50);
        p.setCategory(cat);
        productRepository.save(p);

        mockMvc.perform(get("/api/products?search=fizzy"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Cola"));
    }

    @Test
    void filtersProductsByCategory() throws Exception {
        Category drinks = categoryRepository.save(new Category("Drinks"));
        Category snacks = categoryRepository.save(new Category("Snacks"));

        Product cola = new Product();
        cola.setName("Cola");
        cola.setPrice(new BigDecimal("1.99"));
        cola.setStockQuantity(50);
        cola.setCategory(drinks);
        productRepository.save(cola);

        Product chips = new Product();
        chips.setName("Chips");
        chips.setPrice(new BigDecimal("2.99"));
        chips.setStockQuantity(30);
        chips.setCategory(snacks);
        productRepository.save(chips);

        mockMvc.perform(get("/api/products?categoryId=" + snacks.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Chips"));
    }

    @Test
    void productIncludesDerivedAvailableQuantity() throws Exception {
        Category cat = categoryRepository.save(new Category("Drinks"));
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(50);
        p.setReservedQuantity(5);
        p.setCategory(cat);
        productRepository.save(p);

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].availableQuantity").value(45))
            .andExpect(jsonPath("$.content[0].stockQuantity").value(50))
            .andExpect(jsonPath("$.content[0].reservedQuantity").value(5));
    }
}

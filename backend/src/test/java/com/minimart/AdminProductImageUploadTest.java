package com.minimart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimart.entity.Product;
import com.minimart.repository.CartItemRepository;
import com.minimart.repository.CartRepository;
import com.minimart.repository.CustomerOrderRepository;
import com.minimart.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminProductImageUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        customerOrderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();

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
    void uploadJpegImage() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        productRepository.save(p);

        MockMultipartFile file = new MockMultipartFile(
            "file", "cola.jpg", "image/jpeg", "fake-image-data".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/products/{id}/image", p.getId())
                .file(file)
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.startsWith("/uploads/product-" + p.getId() + "-")));

        Product updated = productRepository.findById(p.getId()).orElseThrow();
        assert updated.getImageUrl().startsWith("/uploads/product-" + p.getId() + "-");
    }

    @Test
    void uploadPngImage() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        productRepository.save(p);

        MockMultipartFile file = new MockMultipartFile(
            "file", "cola.png", "image/png", "fake-image-data".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/products/{id}/image", p.getId())
                .file(file)
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.containsString(".png")));
    }

    @Test
    void uploadWebpImage() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        productRepository.save(p);

        MockMultipartFile file = new MockMultipartFile(
            "file", "cola.webp", "image/webp", "fake-image-data".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/products/{id}/image", p.getId())
                .file(file)
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.containsString(".webp")));
    }

    @Test
    void unsupportedImageTypeIsRejected() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        productRepository.save(p);

        MockMultipartFile file = new MockMultipartFile(
            "file", "cola.gif", "image/gif", "fake-image-data".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/products/{id}/image", p.getId())
                .file(file)
                .header("Authorization", auth()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Unsupported image type. Accepted: JPEG, PNG, WebP"));
    }

    @Test
    void oversizedImageIsRejected() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        productRepository.save(p);

        byte[] largeData = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file", "cola.jpg", "image/jpeg", largeData
        );

        mockMvc.perform(multipart("/api/admin/products/{id}/image", p.getId())
                .file(file)
                .header("Authorization", auth()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Image must be 5 MB or smaller"));
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "cola.jpg", "image/jpeg", "data".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/products/{id}/image", 1L)
                .file(file))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void imageUrlPersistedOnProduct() throws Exception {
        Product p = new Product();
        p.setName("Cola");
        p.setPrice(new BigDecimal("1.99"));
        p.setStockQuantity(100);
        productRepository.save(p);

        MockMultipartFile file = new MockMultipartFile(
            "file", "cola.jpg", "image/jpeg", "data".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/products/{id}/image", p.getId())
                .file(file)
                .header("Authorization", auth()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/products/{id}", p.getId())
                .header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.startsWith("/uploads/product-" + p.getId() + "-")));
    }

    @Test
    void uploadRejectedForNonexistentProduct() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "cola.jpg", "image/jpeg", "data".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/products/{id}/image", 9999L)
                .file(file)
                .header("Authorization", auth()))
            .andExpect(status().isNotFound());
    }
}

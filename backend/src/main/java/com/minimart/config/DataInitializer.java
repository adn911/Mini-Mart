package com.minimart.config;

import com.minimart.entity.AdminUser;
import com.minimart.entity.Category;
import com.minimart.entity.Product;
import com.minimart.repository.AdminUserRepository;
import com.minimart.repository.CategoryRepository;
import com.minimart.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AdminUserRepository adminUserRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           AdminUserRepository adminUserRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (adminUserRepository.count() == 0) {
            adminUserRepository.save(
                new AdminUser("admin", new BCryptPasswordEncoder().encode("admin123")));
        }
        if (categoryRepository.count() > 0) return;

        Category beverages = categoryRepository.save(new Category("Beverages"));
        Category snacks = categoryRepository.save(new Category("Snacks"));
        Category pantry = categoryRepository.save(new Category("Pantry"));
        Category dairy = categoryRepository.save(new Category("Dairy & Eggs"));
        Category bakery = categoryRepository.save(new Category("Bakery"));
        Category frozen = categoryRepository.save(new Category("Frozen"));
        Category produce = categoryRepository.save(new Category("Produce"));
        Category meat = categoryRepository.save(new Category("Meat & Seafood"));

        productRepository.save(createProduct("Sparkling Water", "Premium sparkling mineral water, 12-pack", new BigDecimal("4.99"), 100, beverages, "https://images.unsplash.com/photo-1637905351378-67232a5f0c9b?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Cold Brew Coffee", "Smooth cold brew concentrate, 32oz", new BigDecimal("8.99"), 50, beverages, "https://images.unsplash.com/photo-1777464026595-2521bb394fab?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Green Tea", "Organic Japanese green tea, 20 bags", new BigDecimal("3.49"), 80, beverages, "https://images.unsplash.com/photo-1556881286-fc6915169721?w=400&h=400&fit=crop"));

        productRepository.save(createProduct("Almonds", "Roasted salted almonds, 16oz", new BigDecimal("6.99"), 120, snacks, "https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Dark Chocolate Bar", "72% cacao dark chocolate", new BigDecimal("4.49"), 90, snacks, "https://images.unsplash.com/photo-1575377427642-087cf684f29d?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Trail Mix", "Mixed nuts, seeds, and dried fruit, 12oz", new BigDecimal("5.99"), 60, snacks, "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=400&h=400&fit=crop"));

        productRepository.save(createProduct("Olive Oil", "Extra virgin olive oil, 500ml", new BigDecimal("12.99"), 40, pantry, "https://images.unsplash.com/photo-1757801333175-65177bd6969c?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Basmati Rice", "Aged basmati rice, 2lb", new BigDecimal("7.49"), 70, pantry, "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Pasta", "Artisan durum wheat spaghetti, 1lb", new BigDecimal("3.99"), 150, pantry, "https://images.unsplash.com/photo-1751182471056-ecd29a41f339?w=400&h=400&fit=crop"));

        productRepository.save(createProduct("Whole Milk", "Fresh whole milk, 1 gallon", new BigDecimal("4.29"), 60, dairy, "https://images.unsplash.com/photo-1688267224124-aa10a75ec732?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Greek Yogurt", "Plain Greek yogurt, 32oz", new BigDecimal("5.49"), 45, dairy, "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Free-Range Eggs", "Large brown eggs, dozen", new BigDecimal("6.99"), 80, dairy, "https://images.unsplash.com/photo-1660224286794-fc173fa9295c?w=400&h=400&fit=crop"));

        productRepository.save(createProduct("Sourdough Loaf", "Artisan sourdough bread", new BigDecimal("5.99"), 30, bakery, "https://images.unsplash.com/photo-1505418949117-f2176f78c47a?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Croissants", "Butter croissants, 4-pack", new BigDecimal("4.99"), 40, bakery, "https://images.unsplash.com/photo-1623334044303-241021148842?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Blueberry Muffins", "Fresh-baked blueberry muffins, 2-pack", new BigDecimal("3.49"), 35, bakery, "https://images.unsplash.com/photo-1607958996333-41aef7caefaa?w=400&h=400&fit=crop"));

        productRepository.save(createProduct("Frozen Pizza", "Margherita frozen pizza, 12-inch", new BigDecimal("7.99"), 50, frozen, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Vanilla Ice Cream", "Premium vanilla bean ice cream, 1qt", new BigDecimal("5.99"), 40, frozen, "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Frozen Peas", "Sweet garden peas, 1lb", new BigDecimal("2.99"), 100, frozen, "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=400&h=400&fit=crop"));

        productRepository.save(createProduct("Bananas", "Organic bananas, bunch", new BigDecimal("1.49"), 200, produce, "https://images.unsplash.com/photo-1533675959813-89562fb90120?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Avocados", "Ripe Hass avocados, 2-pack", new BigDecimal("3.99"), 60, produce, "https://images.unsplash.com/photo-1553806527-23657e939f76?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Cherry Tomatoes", "Vine-ripened cherry tomatoes, 1pt", new BigDecimal("3.49"), 70, produce, "https://images.unsplash.com/photo-1749776016433-229308a6b262?w=400&h=400&fit=crop"));

        productRepository.save(createProduct("Chicken Breast", "Boneless skinless chicken breast, 1lb", new BigDecimal("8.99"), 50, meat, "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Atlantic Salmon", "Fresh Atlantic salmon fillet, 8oz", new BigDecimal("12.99"), 30, meat, "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=400&h=400&fit=crop"));
        productRepository.save(createProduct("Ground Beef", "85/15 lean ground beef, 1lb", new BigDecimal("7.49"), 60, meat, "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=400&h=400&fit=crop"));

        productRepository.findByName("Sparkling Water").ifPresent(p -> { p.setDiscountPercent(20); productRepository.save(p); });
        productRepository.findByName("Dark Chocolate Bar").ifPresent(p -> { p.setDiscountPercent(20); productRepository.save(p); });
        productRepository.findByName("Whole Milk").ifPresent(p -> { p.setDiscountPercent(15); productRepository.save(p); });
        productRepository.findByName("Croissants").ifPresent(p -> { p.setDiscountPercent(30); productRepository.save(p); });
        productRepository.findByName("Frozen Pizza").ifPresent(p -> { p.setDiscountPercent(25); productRepository.save(p); });
    }

    private Product createProduct(String name, String description, BigDecimal price, int stockQuantity, Category category, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        return product;
    }
}

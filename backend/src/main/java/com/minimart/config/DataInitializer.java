package com.minimart.config;

import com.minimart.entity.Category;
import com.minimart.entity.Product;
import com.minimart.repository.CategoryRepository;
import com.minimart.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) return;

        Category beverages = categoryRepository.save(new Category("Beverages"));
        Category snacks = categoryRepository.save(new Category("Snacks"));
        Category pantry = categoryRepository.save(new Category("Pantry"));
        Category dairy = categoryRepository.save(new Category("Dairy & Eggs"));
        Category bakery = categoryRepository.save(new Category("Bakery"));
        Category frozen = categoryRepository.save(new Category("Frozen"));
        Category produce = categoryRepository.save(new Category("Produce"));
        Category meat = categoryRepository.save(new Category("Meat & Seafood"));

        productRepository.save(createProduct("Sparkling Water", "Premium sparkling mineral water, 12-pack", new BigDecimal("4.99"), 100, beverages, "https://picsum.photos/seed/beverage1/400/400"));
        productRepository.save(createProduct("Cold Brew Coffee", "Smooth cold brew concentrate, 32oz", new BigDecimal("8.99"), 50, beverages, "https://picsum.photos/seed/beverage2/400/400"));
        productRepository.save(createProduct("Green Tea", "Organic Japanese green tea, 20 bags", new BigDecimal("3.49"), 80, beverages, "https://picsum.photos/seed/beverage3/400/400"));

        productRepository.save(createProduct("Almonds", "Roasted salted almonds, 16oz", new BigDecimal("6.99"), 120, snacks, "https://picsum.photos/seed/snack1/400/400"));
        productRepository.save(createProduct("Dark Chocolate Bar", "72% cacao dark chocolate", new BigDecimal("4.49"), 90, snacks, "https://picsum.photos/seed/snack2/400/400"));
        productRepository.save(createProduct("Trail Mix", "Mixed nuts, seeds, and dried fruit, 12oz", new BigDecimal("5.99"), 60, snacks, "https://picsum.photos/seed/snack3/400/400"));

        productRepository.save(createProduct("Olive Oil", "Extra virgin olive oil, 500ml", new BigDecimal("12.99"), 40, pantry, "https://picsum.photos/seed/pantry1/400/400"));
        productRepository.save(createProduct("Basmati Rice", "Aged basmati rice, 2lb", new BigDecimal("7.49"), 70, pantry, "https://picsum.photos/seed/pantry2/400/400"));
        productRepository.save(createProduct("Pasta", "Artisan durum wheat spaghetti, 1lb", new BigDecimal("3.99"), 150, pantry, "https://picsum.photos/seed/pantry3/400/400"));

        productRepository.save(createProduct("Whole Milk", "Fresh whole milk, 1 gallon", new BigDecimal("4.29"), 60, dairy, "https://picsum.photos/seed/dairy1/400/400"));
        productRepository.save(createProduct("Greek Yogurt", "Plain Greek yogurt, 32oz", new BigDecimal("5.49"), 45, dairy, "https://picsum.photos/seed/dairy2/400/400"));
        productRepository.save(createProduct("Free-Range Eggs", "Large brown eggs, dozen", new BigDecimal("6.99"), 80, dairy, "https://picsum.photos/seed/dairy3/400/400"));

        productRepository.save(createProduct("Sourdough Loaf", "Artisan sourdough bread", new BigDecimal("5.99"), 30, bakery, "https://picsum.photos/seed/bakery1/400/400"));
        productRepository.save(createProduct("Croissants", "Butter croissants, 4-pack", new BigDecimal("4.99"), 40, bakery, "https://picsum.photos/seed/bakery2/400/400"));
        productRepository.save(createProduct("Blueberry Muffins", "Fresh-baked blueberry muffins, 2-pack", new BigDecimal("3.49"), 35, bakery, "https://picsum.photos/seed/bakery3/400/400"));

        productRepository.save(createProduct("Frozen Pizza", "Margherita frozen pizza, 12-inch", new BigDecimal("7.99"), 50, frozen, "https://picsum.photos/seed/frozen1/400/400"));
        productRepository.save(createProduct("Vanilla Ice Cream", "Premium vanilla bean ice cream, 1qt", new BigDecimal("5.99"), 40, frozen, "https://picsum.photos/seed/frozen2/400/400"));
        productRepository.save(createProduct("Frozen Peas", "Sweet garden peas, 1lb", new BigDecimal("2.99"), 100, frozen, "https://picsum.photos/seed/frozen3/400/400"));

        productRepository.save(createProduct("Bananas", "Organic bananas, bunch", new BigDecimal("1.49"), 200, produce, "https://picsum.photos/seed/produce1/400/400"));
        productRepository.save(createProduct("Avocados", "Ripe Hass avocados, 2-pack", new BigDecimal("3.99"), 60, produce, "https://picsum.photos/seed/produce2/400/400"));
        productRepository.save(createProduct("Cherry Tomatoes", "Vine-ripened cherry tomatoes, 1pt", new BigDecimal("3.49"), 70, produce, "https://picsum.photos/seed/produce3/400/400"));

        productRepository.save(createProduct("Chicken Breast", "Boneless skinless chicken breast, 1lb", new BigDecimal("8.99"), 50, meat, "https://picsum.photos/seed/meat1/400/400"));
        productRepository.save(createProduct("Atlantic Salmon", "Fresh Atlantic salmon fillet, 8oz", new BigDecimal("12.99"), 30, meat, "https://picsum.photos/seed/meat2/400/400"));
        productRepository.save(createProduct("Ground Beef", "85/15 lean ground beef, 1lb", new BigDecimal("7.49"), 60, meat, "https://picsum.photos/seed/meat3/400/400"));
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

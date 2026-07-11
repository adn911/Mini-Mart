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

        productRepository.save(createProduct("Sparkling Water", "Premium sparkling mineral water, 12-pack", new BigDecimal("4.99"), 100, beverages, "https://picsum.photos/seed/beverage1/400/400"));
        productRepository.save(createProduct("Cold Brew Coffee", "Smooth cold brew concentrate, 32oz", new BigDecimal("8.99"), 50, beverages, "https://picsum.photos/seed/beverage2/400/400"));
        productRepository.save(createProduct("Green Tea", "Organic Japanese green tea, 20 bags", new BigDecimal("3.49"), 80, beverages, "https://picsum.photos/seed/beverage3/400/400"));

        productRepository.save(createProduct("Almonds", "Roasted salted almonds, 16oz", new BigDecimal("6.99"), 120, snacks, "https://picsum.photos/seed/snack1/400/400"));
        productRepository.save(createProduct("Dark Chocolate Bar", "72% cacao dark chocolate", new BigDecimal("4.49"), 90, snacks, "https://picsum.photos/seed/snack2/400/400"));
        productRepository.save(createProduct("Trail Mix", "Mixed nuts, seeds, and dried fruit, 12oz", new BigDecimal("5.99"), 60, snacks, "https://picsum.photos/seed/snack3/400/400"));

        productRepository.save(createProduct("Olive Oil", "Extra virgin olive oil, 500ml", new BigDecimal("12.99"), 40, pantry, "https://picsum.photos/seed/pantry1/400/400"));
        productRepository.save(createProduct("Basmati Rice", "Aged basmati rice, 2lb", new BigDecimal("7.49"), 70, pantry, "https://picsum.photos/seed/pantry2/400/400"));
        productRepository.save(createProduct("Pasta", "Artisan durum wheat spaghetti, 1lb", new BigDecimal("3.99"), 150, pantry, "https://picsum.photos/seed/pantry3/400/400"));
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

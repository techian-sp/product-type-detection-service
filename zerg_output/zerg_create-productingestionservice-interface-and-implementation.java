```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MetricsLogger metricsLogger;

    @Autowired
    public ProductService(ProductRepository productRepository, MetricsLogger metricsLogger) {
        this.productRepository = productRepository;
        this.metricsLogger = metricsLogger;
    }

    @Transactional
    public void processProducts(List<Product> products) {
        var validProducts = products.stream()
                .filter(this::isValidProduct)
                .collect(Collectors.toSet());

        var deduplicatedProducts = deduplicateProducts(validProducts);

        productRepository.saveAll(deduplicatedProducts);

        metricsLogger.log("Processed ${deduplicatedProducts.size()} products.");
    }

    private boolean isValidProduct(Product product) {
        return switch (product) {
            case Product p when p.name() != null && !p.name().isBlank() && p.price() > 0 -> true;
            default -> false;
        };
    }

    private Set<Product> deduplicateProducts(Set<Product> products) {
        return products.stream()
                .collect(Collectors.toMap(Product::id, p -> p, (existing, replacement) -> existing))
                .values()
                .stream()
                .collect(Collectors.toSet());
    }
}

interface ProductRepository {
    void saveAll(Set<Product> products);
}

interface MetricsLogger {
    void log(String message);
}

record Product(Long id, String name, double price) {}
```
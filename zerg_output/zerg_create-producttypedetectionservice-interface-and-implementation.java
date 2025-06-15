```java
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductTypeService {

    private static final Logger logger = LoggerFactory.getLogger(ProductTypeService.class);

    @Cacheable("productTypeCache")
    public ProductType detectProductType(Product product) {
        logger.info("Starting product type detection for product: {}", product);
        var productType = classifyProduct(product);
        logger.info("Detected product type: {}", productType);
        return productType;
    }

    private ProductType classifyProduct(Product product) {
        return switch (product) {
            case Product(String name, double price) when price > 1000 -> ProductType.PREMIUM;
            case Product(String name, double price) when price > 500 -> ProductType.STANDARD;
            case Product(String name, double price) -> ProductType.BASIC;
        };
    }

    public record Product(String name, double price) {}

    public enum ProductType {
        PREMIUM, STANDARD, BASIC
    }
}
```
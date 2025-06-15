```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    Optional<ProductImage> findById(Long id);

    List<ProductImage> findByProductId(Long productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.url LIKE :urlPattern")
    List<ProductImage> findByUrlPattern(@Param("urlPattern") String urlPattern);

    default String getImageInfo(ProductImage image) {
        return switch (image) {
            case ProductImage(Long id, String url, Long productId) -> 
                String.format("Image ID: %d, URL: %s, Product ID: %d", id, url, productId);
            default -> "Unknown Image";
        };
    }
}

record ProductImage(Long id, String url, Long productId) {}
```
```java
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findBySkuId(String skuId);
    Product findByStyleId(String styleId);
    List<Product> findBySkuIdIn(List<String> skuIds);
}
```
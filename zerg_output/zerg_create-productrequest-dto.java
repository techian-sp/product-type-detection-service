```java
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductDTO(
    @NotNull @Size(min = 1) String styleId,
    @NotNull @Size(min = 1) String skuId,
    @NotNull @Size(min = 1) String title,
    @NotNull @Size(min = 1) String description,
    @NotNull @Size(min = 1) String brand,
    @NotNull @Size(min = 1) List<@NotNull @Size(min = 1) String> imageUrls
) {}
```
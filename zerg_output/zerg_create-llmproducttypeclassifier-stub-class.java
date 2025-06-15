```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class LlmClassifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(LlmClassifierApplication.class, args);
    }
}

@RestController
class ClassifierController {

    @GetMapping("/classify")
    public ClassificationResult classify(@RequestParam String input) {
        return new ClassificationResult(input, classifyInput(input));
    }

    private String classifyInput(String input) {
        return switch (input) {
            case String s when s.isBlank() -> "Empty Input";
            case String s when s.length() < 10 -> "Short Input";
            default -> "General Input";
        };
    }
}

record ClassificationResult(String input, String classification) {}
```
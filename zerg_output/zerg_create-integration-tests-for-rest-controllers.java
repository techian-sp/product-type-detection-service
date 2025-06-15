```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = SampleController.class)
class SampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetSampleData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/sample")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, World!"))
                .andExpect(jsonPath("$.status").value("success"));
    }
}

@RestController
@RequestMapping("/api")
class SampleController {

    @GetMapping("/sample")
    public SampleResponse getSampleData() {
        return new SampleResponse("Hello, World!", "success");
    }
}

record SampleResponse(String message, String status) {}
```
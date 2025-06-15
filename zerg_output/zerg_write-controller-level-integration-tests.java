```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Setup code if needed
    }

    @Test
    void testValidDetectionApi() throws Exception {
        var validPayload = """
                {
                    "type": "detection",
                    "data": "validData"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/detection")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "status": "success",
                            "message": "Detection processed successfully"
                        }
                        """));
    }

    @Test
    void testInvalidDetectionApi() throws Exception {
        var invalidPayload = """
                {
                    "type": "detection",
                    "data": "invalidData"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/detection")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {
                            "status": "error",
                            "message": "Invalid detection data"
                        }
                        """));
    }

    @Test
    void testErrorDetectionApi() throws Exception {
        var errorPayload = """
                {
                    "type": "detection",
                    "data": "errorData"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/detection")
                .contentType(MediaType.APPLICATION_JSON)
                .content(errorPayload))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("""
                        {
                            "status": "error",
                            "message": "Internal server error"
                        }
                        """));
    }

    @Test
    void testValidIngestionApi() throws Exception {
        var validPayload = """
                {
                    "type": "ingestion",
                    "data": "validData"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ingestion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "status": "success",
                            "message": "Ingestion processed successfully"
                        }
                        """));
    }

    @Test
    void testInvalidIngestionApi() throws Exception {
        var invalidPayload = """
                {
                    "type": "ingestion",
                    "data": "invalidData"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ingestion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        {
                            "status": "error",
                            "message": "Invalid ingestion data"
                        }
                        """));
    }

    @Test
    void testErrorIngestionApi() throws Exception {
        var errorPayload = """
                {
                    "type": "ingestion",
                    "data": "errorData"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ingestion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(errorPayload))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("""
                        {
                            "status": "error",
                            "message": "Internal server error"
                        }
                        """));
    }
}
```
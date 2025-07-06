package be.pxl.research.config;

import be.pxl.research.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtUtil.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowedUrl_registerEndpoint_shouldReturnOk() throws Exception {
        String requestBody = """
    {
      "username": "Alice",
      "email": "alice@example.com",
      "password": "password123"
    }
    """;

        mockMvc.perform(post("/api/users/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void allowedUrl_loginEndpoint_shouldReturnOk() throws Exception {
        String requestBody = """
        {
          "email": "john@example.com",
          "password": "password123"
        }
        """;

        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void corsConfiguration_shouldAllowOptionsRequest() throws Exception {
        mockMvc.perform(options("/api/users/register")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Origin", "http://localhost:8080"))
                .andExpect(status().isOk());
    }

    @Test
    void restrictedUrl_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/secure/endpoint"))
                .andExpect(status().isForbidden());
    }
}

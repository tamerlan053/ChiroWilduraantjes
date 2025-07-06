package be.pxl.research.controller;

import be.pxl.research.controller.request.UserLoginRequest;
import be.pxl.research.controller.request.UserRegistrationRequest;
import be.pxl.research.domain.User;
import be.pxl.research.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SecurityFilterChain securityFilterChain;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loginUser_Success() throws Exception {
        UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", "password");
        Mockito.when(userService.loginUser(any())).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-jwt-token"));
    }

    @Test
    void loginUser_InvalidCredentials_ReturnsBadRequest() throws Exception {
        UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", "wrong");
        Mockito.when(userService.loginUser(any())).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Credentials"));
    }

    @Test
    void registerUser_SuccessfulRegistration_ReturnsCreated() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("newuser", "securepassword");

        User mockUser = new User();
        mockUser.setUsername("newuser");

        Mockito.when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenReturn(mockUser);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));
    }


    @Test
    void registerUser_BlankUsername_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("", "password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"));
    }

    @Test
    void registerUser_BlankPassword_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("user123", "");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password is required"));
    }

    @Test
    void registerUser_UsernameAlreadyExists_ReturnsBadRequest() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("existinguser", "password");

        Mockito.doThrow(new RuntimeException("Username already exists"))
                .when(userService).registerUser(any(UserRegistrationRequest.class));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }
}
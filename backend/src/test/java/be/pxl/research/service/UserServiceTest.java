package be.pxl.research.service;

import be.pxl.research.controller.request.UserLoginRequest;
import be.pxl.research.controller.request.UserRegistrationRequest;
import be.pxl.research.domain.Roles;
import be.pxl.research.domain.User;
import be.pxl.research.repository.UserRepository;
import be.pxl.research.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password123";
    private final String ENCODED_PASSWORD = "encoded_password";
    private final String TEST_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword(ENCODED_PASSWORD);
        Set<Roles> roles = new HashSet<>();
        roles.add(Roles.CASHIER);
        testUser.setRoles(roles);
    }

    @Test
    void loginUser_SuccessfulLogin_ReturnsToken() {
        UserLoginRequest loginRequest = new UserLoginRequest(TEST_USERNAME, TEST_PASSWORD);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(eq(TEST_USERNAME), any(Set.class))).thenReturn(TEST_TOKEN);

        String result = userService.loginUser(loginRequest);

        assertEquals(TEST_TOKEN, result);
        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
        verify(passwordEncoder, times(1)).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        verify(jwtUtil, times(1)).generateToken(eq(TEST_USERNAME), any(Set.class));
    }

    @Test
    void loginUser_UserNotFound_ReturnsDone() {
        UserLoginRequest loginRequest = new UserLoginRequest("nonexistent", TEST_PASSWORD);

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        String result = userService.loginUser(loginRequest);

        assertEquals("done", result);
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }

    @Test
    void registerUser_SuccessfulRegistration_ReturnsNewUser() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(TEST_USERNAME, TEST_PASSWORD);
        User newUser = new User();
        newUser.setUsername(TEST_USERNAME);
        newUser.setPassword(ENCODED_PASSWORD);
        newUser.setRoles(new HashSet<>());

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.registerUser(registrationRequest);

        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
        assertTrue(result.getRoles().isEmpty());

        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_UsernameAlreadyExists_ThrowsException() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(TEST_USERNAME, TEST_PASSWORD);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationRequest);
        });

        assertEquals("Username is already in use", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(TEST_USERNAME);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_SetsEmptyRolesSet() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest("newuser", TEST_PASSWORD);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        User result = userService.registerUser(registrationRequest);

        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
    }
}

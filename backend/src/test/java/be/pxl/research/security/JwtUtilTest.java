package be.pxl.research.security;

import be.pxl.research.domain.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateToken_shouldContainUsernameAndRoles() {
        String token = jwtUtil.generateToken("john", Set.of(Roles.KITCHEN, Roles.ADMIN));

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("john");
        assertThat(jwtUtil.extractRoles(token)).containsExactlyInAnyOrder("KITCHEN", "ADMIN");
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken("john", Set.of(Roles.HALL));

        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String invalidToken = "this.is.not.a.valid.token";

        assertThat(jwtUtil.validateToken(invalidToken)).isFalse();
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken("alice", Set.of(Roles.CASHIER));

        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo("alice");
    }

    @Test
    void extractRoles_shouldReturnCorrectRoles() {
        String token = jwtUtil.generateToken("bob", Set.of(Roles.ADMIN, Roles.HALL));

        List<String> roles = jwtUtil.extractRoles(token);

        assertThat(roles).containsExactlyInAnyOrder("ADMIN", "HALL");
    }

    @Test
    void getAuthentication_shouldReturnAuthenticationWithCorrectAuthorities() {
        String token = jwtUtil.generateToken("bob", Set.of(Roles.KITCHEN, Roles.CASHIER));

        Authentication authentication = jwtUtil.getAuthentication(token);

        assertThat(authentication.getName()).isEqualTo("bob");
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_KITCHEN", "ROLE_CASHIER");
    }
}
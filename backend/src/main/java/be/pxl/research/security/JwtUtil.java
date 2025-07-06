package be.pxl.research.security;

import be.pxl.research.domain.Roles;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "superSecretJWTkey";
    private static final long EXPIRATION_MS =  86400000;
    private static final String ROLES_STRING = "roles";

    public String generateToken(String username, Set<Roles> roles) {
        List<String> roleNames = roles.stream()
                .map(Enum::name)
                .toList();

        return Jwts.builder()
                .setSubject(username)
                .claim(ROLES_STRING, roleNames) // Store roles without prefix
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }


    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        return extractClaims(token).get(ROLES_STRING, List.class);
    }

    public boolean validateToken(String token) {
        try{
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);
            return true;
        }
        catch (Exception e) {
            return false;
            }
    }


    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = extractClaims(token);

        String username = claims.getSubject();
        List<String> roles = claims.get(ROLES_STRING, List.class); // Roles uit de token halen

        Set<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Ensure the prefix is added
                .collect(Collectors.toSet());

        UserDetails userDetails = new User(username, "", authorities);

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }
}

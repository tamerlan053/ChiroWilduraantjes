package be.pxl.research.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Roles implements GrantedAuthority {
    KITCHEN,
    ADMIN,
    CASHIER,
    HALL;

    @Override
    public String getAuthority() {
        return "ROLE_" + name(); // Returns 'KITCHEN', 'CASHIER', etc...
    }
}

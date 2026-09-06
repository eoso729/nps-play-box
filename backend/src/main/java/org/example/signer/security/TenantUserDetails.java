package org.example.signer.security;

import lombok.Getter;
import org.example.signer.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserDetails implementation encapsulating Spring Security contract
 * alongside multi-tenant identity metadata.
 */
@Getter
public class TenantUserDetails implements UserDetails {

    private final User user;
    private final UUID userUuid;
    private final String email;
    private final Long tenantId;
    private final String tenantSlug;
    private final User.UserRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public TenantUserDetails(User user, String tenantSlug) {
        this.user = user;
        this.userUuid = user.getUserUuid();
        this.email = user.getEmail();
        this.tenantId = user.getTenantId();
        this.tenantSlug = tenantSlug;
        this.role = user.getRole();
        String roleName = user.getRole() != null ? user.getRole().name() : "VIEWER";
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUserUuid() != null ? user.getUserUuid().toString() : user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != User.UserStatus.INACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == User.UserStatus.ACTIVE;
    }
}

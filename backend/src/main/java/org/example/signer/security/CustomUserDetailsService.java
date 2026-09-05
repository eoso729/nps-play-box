package org.example.signer.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Loads UserDetails by user UUID or fallback username/email, attaching tenant metadata.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = null;

        try {
            UUID userUuid = UUID.fromString(identifier);
            user = userRepository.findByUserUuid(userUuid).orElse(null);
        } catch (IllegalArgumentException ignored) {
            // Identifier is not a UUID, proceed to username/email lookup
        }

        if (user == null) {
            user = userRepository.findByEmailOrUsername(identifier, identifier)
                    .orElseGet(() -> userRepository.findByEmail(identifier)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier)));
        }

        String tenantSlug = "platform-admin";
        if (user.getTenantId() != null) {
            tenantSlug = tenantRepository.findById(user.getTenantId())
                    .map(Tenant::getSlug)
                    .orElse("platform-admin");
        }

        return new TenantUserDetails(user, tenantSlug);
    }
}

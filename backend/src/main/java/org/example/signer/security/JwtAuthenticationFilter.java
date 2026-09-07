package org.example.signer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import org.example.signer.entity.ImpersonationSession;
import org.example.signer.repository.ImpersonationSessionRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Filter that intercepts incoming HTTP requests, extracts and validates JWT tokens,
 * sets up Spring Security context, and manages ThreadLocal TenantContext propagation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ImpersonationSessionRepository impersonationSessionRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtService.validateToken(jwt)) {
                boolean isImpersonation = jwtService.isImpersonationToken(jwt);
                if (isImpersonation) {
                    String sessionUuidStr = jwtService.extractSessionUuid(jwt);
                    if (sessionUuidStr == null) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid impersonation session\"}");
                        return;
                    }
                    try {
                        UUID sessionUuid = UUID.fromString(sessionUuidStr);
                        Optional<ImpersonationSession> sessionOpt = impersonationSessionRepository.findBySessionUuid(sessionUuid);
                        if (sessionOpt.isEmpty() || !sessionOpt.get().isActive()) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Impersonation session expired or terminated\"}");
                            return;
                        }
                    } catch (IllegalArgumentException ex) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid impersonation session UUID\"}");
                        return;
                    }
                }

                String identifier = jwtService.extractUsername(jwt);
                Long tenantId = jwtService.extractTenantId(jwt);
                String tenantSlug = jwtService.extractTenantSlug(jwt);

                if (identifier != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);

                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Populate TenantContext and request attributes
                        TenantContext.setTenantId(tenantId);
                        TenantContext.setTenantSlug(tenantSlug);
                        request.setAttribute("tenantId", tenantId);
                        request.setAttribute("tenantSlug", tenantSlug);
                        request.setAttribute("isImpersonation", isImpersonation);
                        request.setAttribute("impersonated", isImpersonation);
                        if (isImpersonation) {
                            request.setAttribute("supportUserId", jwtService.extractSupportUserId(jwt));
                            request.setAttribute("sessionUuid", jwtService.extractSessionUuid(jwt));
                        }
                        if (userDetails instanceof TenantUserDetails tud && tud.getUser() != null) {
                            request.setAttribute("userId", tud.getUser().getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}

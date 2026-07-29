package org.example.signer.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.model.User;
import org.example.signer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String oid = getAttributeAsString(oAuth2User, "oid", "sub", "id");
        String email = getAttributeAsString(oAuth2User, "email", "preferred_username", "userPrincipalName", "mail");
        String name = getAttributeAsString(oAuth2User, "name", "displayName");

        if (email == null && oid != null) {
            email = oid + "@microsoft.oauth";
        }

        String baseUsername = email != null ? email.split("@")[0] : ("ms_" + oid);

        log.info("Microsoft OAuth2 login successful for email: {}, oid: {}", email, oid);

        String finalEmail = email;
        String finalOid = oid;

        User user = userRepository.findByMicrosoftOid(oid)
                .orElseGet(() -> userRepository.findByEmail(finalEmail)
                        .orElseGet(() -> {
                            String uniqueUsername = baseUsername;
                            int count = 1;
                            while (userRepository.existsByUsername(uniqueUsername)) {
                                uniqueUsername = baseUsername + count++;
                            }

                            return User.builder()
                                    .username(uniqueUsername)
                                    .email(finalEmail)
                                    .authProvider("MICROSOFT")
                                    .microsoftOid(finalOid)
                                    .role("ROLE_USER")
                                    .firstName(name)
                                    .createdAt(LocalDateTime.now())
                                    .build();
                        }));

        user.setLastLoginAt(LocalDateTime.now());
        if (user.getMicrosoftOid() == null) {
            user.setMicrosoftOid(oid);
        }

        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(savedUser);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("expiresIn", jwtUtils.getExpirationMs())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getAttributeAsString(OAuth2User oAuth2User, String... attributeNames) {
        for (String attr : attributeNames) {
            Object val = oAuth2User.getAttribute(attr);
            if (val != null) {
                return val.toString();
            }
        }
        return null;
    }
}

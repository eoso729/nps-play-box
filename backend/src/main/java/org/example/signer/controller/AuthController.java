package org.example.signer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.auth.AuthResponseDto;
import org.example.signer.dto.auth.LoginRequestDto;
import org.example.signer.dto.auth.RegisterRequestDto;
import org.example.signer.dto.auth.UserDto;
import org.example.signer.model.User;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Error: Email is already in use!"));
        }

        User user = User.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .passwordHash(passwordEncoder.encode(registerDto.getPassword()))
                .authProvider("LOCAL")
                .role("ROLE_USER")
                .firstName(registerDto.getFirstName())
                .lastName(registerDto.getLastName())
                .organization(registerDto.getOrganization())
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(savedUser);

        AuthResponseDto response = AuthResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationMs())
                .user(UserDto.fromEntity(savedUser))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginDto) {
        User user = userRepository.findByEmailOrUsername(loginDto.getEmailOrUsername(), loginDto.getEmailOrUsername())
                .orElseGet(() -> userRepository.findByUsername(loginDto.getEmailOrUsername())
                        .orElseGet(() -> userRepository.findByEmail(loginDto.getEmailOrUsername()).orElse(null)));

        if (user == null || user.getPasswordHash() == null ||
                !passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email/username or password"));
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtils.generateToken(user);

        AuthResponseDto response = AuthResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpirationMs())
                .user(UserDto.fromEntity(user))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User is not authenticated"));
        }

        Object principal = authentication.getPrincipal();
        User user = null;

        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof String) {
            user = userRepository.findByUsername((String) principal)
                    .orElseGet(() -> userRepository.findByEmail((String) principal).orElse(null));
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
        }

        return ResponseEntity.ok(UserDto.fromEntity(user));
    }
}

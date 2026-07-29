package org.example.signer.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.model.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String authProvider;
    private String role;
    private String firstName;
    private String lastName;
    private String organization;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .authProvider(user.getAuthProvider())
                .role(user.getRole())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .organization(user.getOrganization())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}

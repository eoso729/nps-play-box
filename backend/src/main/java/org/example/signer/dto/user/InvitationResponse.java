package org.example.signer.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private Long id;
    private String email;
    private String role;
    private String invitationToken;
    private String invitedByName;
    private String invitedByEmail;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean expired;
}

package org.example.signer.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.entity.TenantSimulatorProfile;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSimulatorProfileDto {

    private Long id;
    private Long tenantId;
    private UUID profileUuid;
    private String institutionCode;
    private String institutionName;
    private String bic;
    private String schemeCode;
    private String defaultCurrency;
    private String defaultAccountNumber;
    private String defaultAccountName;
    private String defaultBvn;
    private String callbackUrl;
    private Boolean autoRespondInbound;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TenantSimulatorProfileDto fromEntity(TenantSimulatorProfile entity) {
        if (entity == null) return null;
        return TenantSimulatorProfileDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .profileUuid(entity.getProfileUuid())
                .institutionCode(entity.getInstitutionCode())
                .institutionName(entity.getInstitutionName())
                .bic(entity.getBic())
                .schemeCode(entity.getSchemeCode())
                .defaultCurrency(entity.getDefaultCurrency())
                .defaultAccountNumber(entity.getDefaultAccountNumber())
                .defaultAccountName(entity.getDefaultAccountName())
                .defaultBvn(entity.getDefaultBvn())
                .callbackUrl(entity.getCallbackUrl())
                .autoRespondInbound(entity.getAutoRespondInbound())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

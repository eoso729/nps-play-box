package org.example.signer.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSimulatorProfileDto {

    @NotBlank(message = "institutionCode is required")
    private String institutionCode;

    @NotBlank(message = "institutionName is required")
    private String institutionName;

    @NotBlank(message = "bic is required")
    private String bic;

    private String schemeCode;
    private String defaultCurrency;
    private String defaultAccountNumber;
    private String defaultAccountName;
    private String defaultBvn;
    private String callbackUrl;
    private Boolean autoRespondInbound;
}

package org.example.signer.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmlInspectRequestDto {
    private String xmlContent;
    private String messageType; // Optional, auto-detected if omitted
}

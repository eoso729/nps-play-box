package org.example.signer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageSendResponseDto {
    private String messageType;
    private String messageId;
    private String plainXml;
    private String signedXml;
    private ServicePushResult serviceResponse;
}

package org.example.signer.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.entity.Iso20022Message;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageRequestDto {

    @NotNull(message = "messageType is required")
    private Iso20022Message.MessageType messageType;

    @NotBlank(message = "messageCode is required (e.g. pain.001, pacs.008)")
    private String messageCode;

    private Iso20022Message.MessageDirection direction;

    @NotBlank(message = "rawXml is required")
    private String rawXml;

    private String transactionReference;
    private String endToEndId;
    private String messageId;
    private Map<String, Object> metadata;
}

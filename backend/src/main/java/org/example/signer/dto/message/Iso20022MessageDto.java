package org.example.signer.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.entity.Iso20022Message;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Iso20022MessageDto {

    private Long id;
    private UUID messageUuid;
    private Iso20022Message.MessageType messageType;
    private String messageCode;
    private Iso20022Message.MessageDirection direction;
    private Iso20022Message.MessageStatus status;
    private String rawXml;
    private String signedXml;
    private String encryptedXml;
    private Long createdBy;
    private LocalDateTime processedAt;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
    private String transactionReference;
    private String endToEndId;
    private String messageId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> metadata;

    public static Iso20022MessageDto fromEntity(Iso20022Message entity) {
        if (entity == null) return null;
        return Iso20022MessageDto.builder()
                .id(entity.getId())
                .messageUuid(entity.getMessageUuid())
                .messageType(entity.getMessageType())
                .messageCode(entity.getMessageCode())
                .direction(entity.getDirection())
                .status(entity.getStatus())
                .rawXml(entity.getRawXml())
                .signedXml(entity.getSignedXml())
                .encryptedXml(entity.getEncryptedXml())
                .createdBy(entity.getCreatedBy())
                .processedAt(entity.getProcessedAt())
                .sentAt(entity.getSentAt())
                .receivedAt(entity.getReceivedAt())
                .transactionReference(entity.getTransactionReference())
                .endToEndId(entity.getEndToEndId())
                .messageId(entity.getMessageId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .metadata(entity.getMetadata())
                .build();
    }
}

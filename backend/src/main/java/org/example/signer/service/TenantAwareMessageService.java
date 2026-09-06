package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.message.Iso20022MessageDto;
import org.example.signer.dto.message.MessageStatisticsDto;
import org.example.signer.entity.Iso20022Message;
import org.example.signer.exception.ResourceNotFoundException;
import org.example.signer.repository.Iso20022MessageRepository;
import org.example.signer.security.SimulatorKeyProvider;
import org.example.signer.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAwareMessageService {

    private final Iso20022MessageRepository messageRepository;
    private final SimulatorKeyProvider simulatorKeyProvider;

    @Transactional
    public Iso20022MessageDto createDraftMessage(CreateMessageRequestDto requestDto) {
        Long tenantId = resolveTenantId();

        Iso20022Message message = Iso20022Message.builder()
                .tenantId(tenantId)
                .messageType(requestDto.getMessageType())
                .messageCode(requestDto.getMessageCode())
                .direction(requestDto.getDirection() != null ? requestDto.getDirection() : Iso20022Message.MessageDirection.OUTBOUND)
                .status(Iso20022Message.MessageStatus.DRAFT)
                .rawXml(requestDto.getRawXml())
                .transactionReference(requestDto.getTransactionReference())
                .endToEndId(requestDto.getEndToEndId())
                .messageId(requestDto.getMessageId())
                .metadata(requestDto.getMetadata())
                .build();

        Iso20022Message saved = messageRepository.save(message);
        log.info("Created draft ISO 20022 message {} for tenant {}", saved.getMessageUuid(), tenantId);
        return Iso20022MessageDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Iso20022MessageDto getMessageByUuid(UUID messageUuid) {
        Long tenantId = resolveTenantId();
        Iso20022Message message = messageRepository.findByTenantIdAndMessageUuid(tenantId, messageUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Iso20022Message", "uuid", messageUuid));
        return Iso20022MessageDto.fromEntity(message);
    }

    @Transactional(readOnly = true)
    public Page<Iso20022MessageDto> listMessages(
            Pageable pageable,
            Iso20022Message.MessageType messageType,
            Iso20022Message.MessageStatus status,
            Iso20022Message.MessageDirection direction) {

        Long tenantId = resolveTenantId();
        Page<Iso20022Message> page;

        if (messageType != null) {
            page = messageRepository.findByTenantIdAndMessageTypeOrderByCreatedAtDesc(tenantId, messageType, pageable);
        } else if (status != null) {
            page = messageRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status, pageable);
        } else if (direction != null) {
            page = messageRepository.findByTenantIdAndDirectionOrderByCreatedAtDesc(tenantId, direction, pageable);
        } else {
            page = messageRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        }

        return page.map(Iso20022MessageDto::fromEntity);
    }

    @Transactional
    public Iso20022MessageDto signMessage(UUID messageUuid) {
        Long tenantId = resolveTenantId();
        Iso20022Message message = messageRepository.findByTenantIdAndMessageUuid(tenantId, messageUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Iso20022Message", "uuid", messageUuid));

        try {
            String signedXml = simulatorKeyProvider.signXml(message.getRawXml());
            message.setSignedXml(signedXml);
            message.setStatus(Iso20022Message.MessageStatus.SIGNED);
            message.setProcessedAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());

            Iso20022Message saved = messageRepository.save(message);
            log.info("Successfully signed message {} for tenant {}", messageUuid, tenantId);
            return Iso20022MessageDto.fromEntity(saved);
        } catch (Exception e) {
            log.error("Failed to sign message {}: {}", messageUuid, e.getMessage(), e);
            message.setStatus(Iso20022Message.MessageStatus.FAILED);
            messageRepository.save(message);
            throw new RuntimeException("Digital signature failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Iso20022MessageDto encryptMessage(UUID messageUuid, String elementTagName) {
        Long tenantId = resolveTenantId();
        Iso20022Message message = messageRepository.findByTenantIdAndMessageUuid(tenantId, messageUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Iso20022Message", "uuid", messageUuid));

        try {
            String baseXml = message.getSignedXml() != null ? message.getSignedXml() : message.getRawXml();
            String encryptedXml = simulatorKeyProvider.encryptXml(baseXml, elementTagName);

            message.setEncryptedXml(encryptedXml);
            message.setStatus(Iso20022Message.MessageStatus.ENCRYPTED);
            message.setProcessedAt(LocalDateTime.now());
            message.setUpdatedAt(LocalDateTime.now());

            Iso20022Message saved = messageRepository.save(message);
            log.info("Successfully encrypted element '{}' in message {} for tenant {}", 
                    elementTagName, messageUuid, tenantId);
            return Iso20022MessageDto.fromEntity(saved);
        } catch (Exception e) {
            log.error("Failed to encrypt message {}: {}", messageUuid, e.getMessage(), e);
            message.setStatus(Iso20022Message.MessageStatus.FAILED);
            messageRepository.save(message);
            throw new RuntimeException("XML encryption failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Iso20022Message recordOutboundMessage(
            Long tenantId,
            Iso20022Message.MessageType messageType,
            String messageCode,
            String rawXml,
            String signedXml,
            String encryptedXml,
            String messageId,
            String transactionRef,
            String endToEndId,
            Iso20022Message.MessageStatus status) {

        Iso20022Message message = Iso20022Message.builder()
                .tenantId(tenantId)
                .messageType(messageType)
                .messageCode(messageCode)
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml(rawXml)
                .signedXml(signedXml)
                .encryptedXml(encryptedXml)
                .messageId(messageId)
                .transactionReference(transactionRef)
                .endToEndId(endToEndId)
                .status(status != null ? status : Iso20022Message.MessageStatus.SENT)
                .sentAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    @Transactional
    public Iso20022Message handleInboundSimulatorResponse(
            Long tenantId,
            Iso20022Message.MessageType messageType,
            String messageCode,
            String responseXml,
            String originalMessageId,
            String transactionRef,
            String endToEndId) {

        // Correlate with outbound message if found
        if (transactionRef != null) {
            messageRepository.findByTenantIdAndTransactionReference(tenantId, transactionRef)
                    .ifPresent(out -> {
                        out.setStatus(Iso20022Message.MessageStatus.DELIVERED);
                        out.setUpdatedAt(LocalDateTime.now());
                        messageRepository.save(out);
                    });
        }

        Iso20022Message inbound = Iso20022Message.builder()
                .tenantId(tenantId)
                .messageType(messageType)
                .messageCode(messageCode)
                .direction(Iso20022Message.MessageDirection.INBOUND)
                .rawXml(responseXml)
                .messageId(originalMessageId)
                .transactionReference(transactionRef)
                .endToEndId(endToEndId)
                .status(Iso20022Message.MessageStatus.DELIVERED)
                .receivedAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now())
                .build();

        return messageRepository.save(inbound);
    }

    @Transactional(readOnly = true)
    public MessageStatisticsDto getMessageStatistics() {
        Long tenantId = resolveTenantId();
        long total = messageRepository.countByTenantId(tenantId);
        long outbound = messageRepository.countByTenantIdAndDirection(tenantId, Iso20022Message.MessageDirection.OUTBOUND);
        long inbound = messageRepository.countByTenantIdAndDirection(tenantId, Iso20022Message.MessageDirection.INBOUND);

        Map<String, Long> statusDist = new HashMap<>();
        List<Object[]> statusList = messageRepository.countByStatusGrouped(tenantId);
        for (Object[] row : statusList) {
            statusDist.put(String.valueOf(row[0]), (Long) row[1]);
        }

        Map<String, Long> typeDist = new HashMap<>();
        List<Object[]> typeList = messageRepository.countByMessageTypeGrouped(tenantId);
        for (Object[] row : typeList) {
            typeDist.put(String.valueOf(row[0]), (Long) row[1]);
        }

        return MessageStatisticsDto.builder()
                .totalMessages(total)
                .outboundCount(outbound)
                .inboundCount(inbound)
                .statusDistribution(statusDist)
                .typeDistribution(typeDist)
                .build();
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant", "context", "No tenant context available in request");
        }
        return tenantId;
    }
}

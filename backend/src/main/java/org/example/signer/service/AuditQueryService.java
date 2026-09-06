package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import org.example.signer.dto.audit.AuditEventResponse;
import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.User;
import org.example.signer.repository.AuditEventRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.specification.AuditEventSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> searchAuditEvents(
            Long tenantId, AuditEventSearchRequest searchRequest, Pageable pageable) {

        Specification<AuditEvent> spec = AuditEventSpecification.buildSpecification(
                tenantId, searchRequest);

        Page<AuditEvent> events = auditEventRepository.findAll(spec, pageable);

        Map<Long, String> userEmails = loadUserEmails(events.getContent());

        return events.map(event -> mapToResponse(event, userEmails));
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> getResourceHistory(
            Long tenantId, String resourceType, String resourceId) {

        List<AuditEvent> events = auditEventRepository.findResourceHistory(
                tenantId, resourceType, resourceId);

        Map<Long, String> userEmails = loadUserEmails(events);

        return events.stream()
                .map(event -> mapToResponse(event, userEmails))
                .toList();
    }

    private Map<Long, String> loadUserEmails(List<AuditEvent> events) {
        Map<Long, String> emailMap = new HashMap<>();

        List<Long> userIds = events.stream()
                .map(AuditEvent::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (!userIds.isEmpty()) {
            List<User> users = userRepository.findAllById(userIds);
            users.forEach(user -> emailMap.put(user.getId(), user.getEmail()));
        }

        return emailMap;
    }

    private AuditEventResponse mapToResponse(AuditEvent event, Map<Long, String> userEmails) {
        return AuditEventResponse.builder()
                .eventUuid(event.getEventUuid() != null ? event.getEventUuid().toString() : null)
                .tenantId(event.getTenantId())
                .userId(event.getUserId())
                .userEmail(event.getUserId() != null ? userEmails.get(event.getUserId()) : null)
                .eventType(event.getEventType() != null ? event.getEventType().name() : null)
                .action(event.getAction())
                .resourceType(event.getResourceType())
                .resourceId(event.getResourceId())
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .requestId(event.getRequestId())
                .metadata(event.getMetadata())
                .errorMessage(event.getErrorMessage())
                .createdAt(event.getCreatedAt())
                .build();
    }
}

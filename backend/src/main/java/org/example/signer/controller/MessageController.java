package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.message.Iso20022MessageDto;
import org.example.signer.dto.message.MessageStatisticsDto;
import org.example.signer.entity.Iso20022Message;
import org.example.signer.security.RequireDeveloper;
import org.example.signer.service.TenantAwareMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "ISO 20022 Messages", description = "Tenant-isolated message persistence, signing, encryption, and lifecycle management")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final TenantAwareMessageService messageService;

    @Operation(summary = "Create draft ISO 20022 message", description = "Stores raw XML payload scoped to current tenant")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Draft message created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @RequireDeveloper
    @PostMapping
    public ResponseEntity<Iso20022MessageDto> createMessage(@Valid @RequestBody CreateMessageRequestDto requestDto) {
        Iso20022MessageDto created = messageService.createDraftMessage(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get ISO 20022 message by UUID", description = "Retrieves message details strictly within tenant boundaries")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found in tenant")
    })
    @GetMapping("/{messageUuid}")
    public ResponseEntity<Iso20022MessageDto> getMessage(@PathVariable UUID messageUuid) {
        return ResponseEntity.ok(messageService.getMessageByUuid(messageUuid));
    }

    @Operation(summary = "List tenant ISO 20022 messages", description = "Paginated message list with optional filters")
    @ApiResponse(responseCode = "200", description = "Messages listed successfully")
    @GetMapping
    public ResponseEntity<Page<Iso20022MessageDto>> listMessages(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Iso20022Message.MessageType type,
            @RequestParam(required = false) Iso20022Message.MessageStatus status,
            @RequestParam(required = false) Iso20022Message.MessageDirection direction) {

        return ResponseEntity.ok(messageService.listMessages(pageable, type, status, direction));
    }

    @Operation(summary = "Sign message with shared simulator key", description = "Digitally signs the raw XML using NIBSS-compliant simulator key")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message signed successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found in tenant")
    })
    @RequireDeveloper
    @PostMapping("/{messageUuid}/sign")
    public ResponseEntity<Iso20022MessageDto> signMessage(@PathVariable UUID messageUuid) {
        return ResponseEntity.ok(messageService.signMessage(messageUuid));
    }

    @Operation(summary = "Encrypt element in message", description = "Encrypts specified element using shared simulator public key")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Element encrypted successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found in tenant")
    })
    @RequireDeveloper
    @PostMapping("/{messageUuid}/encrypt")
    public ResponseEntity<Iso20022MessageDto> encryptMessage(
            @PathVariable UUID messageUuid,
            @RequestParam(defaultValue = "Body") String elementTagName) {

        return ResponseEntity.ok(messageService.encryptMessage(messageUuid, elementTagName));
    }

    @Operation(summary = "Get message statistics", description = "Aggregated count and status distribution for current tenant")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @GetMapping("/statistics")
    public ResponseEntity<MessageStatisticsDto> getStatistics() {
        return ResponseEntity.ok(messageService.getMessageStatistics());
    }
}

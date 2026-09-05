package org.example.signer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.signer.dto.audit.AuditEventResponse;
import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditExportService {

    private final AuditQueryService auditQueryService;
    private final ObjectMapper objectMapper;

    /**
     * Export audit events as pretty-printed JSON.
     */
    public byte[] exportAsJson(Long tenantId, AuditEventSearchRequest searchRequest, int maxRecords) throws IOException {
        List<AuditEventResponse> allEvents = fetchAllEvents(tenantId, searchRequest, maxRecords);
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(allEvents);
    }

    /**
     * Export audit events as RFC-4180 compliant CSV.
     */
    public byte[] exportAsCsv(Long tenantId, AuditEventSearchRequest searchRequest, int maxRecords) {
        List<AuditEventResponse> allEvents = fetchAllEvents(tenantId, searchRequest, maxRecords);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // Header
        writer.println("Event UUID,Timestamp,Event Type,Action,Resource Type,Resource ID,User ID,User Email,Status,IP Address,User Agent,Request ID,Error Message");

        for (AuditEventResponse event : allEvents) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    escapeCsv(event.getEventUuid()),
                    escapeCsv(event.getCreatedAt() != null ? event.getCreatedAt().toString() : ""),
                    escapeCsv(event.getEventType()),
                    escapeCsv(event.getAction()),
                    escapeCsv(event.getResourceType()),
                    escapeCsv(event.getResourceId()),
                    escapeCsv(event.getUserId() != null ? event.getUserId().toString() : ""),
                    escapeCsv(event.getUserEmail()),
                    escapeCsv(event.getStatus()),
                    escapeCsv(event.getIpAddress()),
                    escapeCsv(event.getUserAgent()),
                    escapeCsv(event.getRequestId()),
                    escapeCsv(event.getErrorMessage())
            ));
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    private List<AuditEventResponse> fetchAllEvents(Long tenantId, AuditEventSearchRequest searchRequest, int maxRecords) {
        List<AuditEventResponse> allEvents = new ArrayList<>();
        int pageSize = Math.min(1000, Math.max(1, maxRecords));
        int page = 0;

        while (allEvents.size() < maxRecords) {
            int remaining = maxRecords - allEvents.size();
            PageRequest pageRequest = PageRequest.of(page, Math.min(pageSize, remaining));
            Page<AuditEventResponse> pageResult = auditQueryService.searchAuditEvents(
                    tenantId, searchRequest, pageRequest);

            allEvents.addAll(pageResult.getContent());

            if (!pageResult.hasNext() || pageResult.isEmpty()) {
                break;
            }
            page++;
        }

        return allEvents;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

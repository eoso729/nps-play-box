package org.example.signer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePushResult {
    private int statusCode;
    private String rawResponseBody;
    private long executionTimeMs;
    private boolean success;
    private String timestamp;
}

package org.example.signer.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmlAutoFixRequestDto {
    private String xmlContent;
    private String messageType;
    private boolean formatOnly;
    private boolean fixIds;
    private boolean fixDates;
    private boolean fixSupplementaryData;
    private boolean truncateOversized;
}

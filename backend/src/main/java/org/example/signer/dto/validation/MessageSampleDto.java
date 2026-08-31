package org.example.signer.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.validation.IsoFieldDef;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSampleDto {
    private String key;
    private String name;
    private String isoCode;
    private String category;
    private String rootElement;
    private String sampleXml;
    private List<IsoFieldDef> fields;
}

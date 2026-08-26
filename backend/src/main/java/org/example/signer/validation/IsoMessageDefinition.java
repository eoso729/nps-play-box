package org.example.signer.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsoMessageDefinition {
    private String key;                 // e.g. "pacs.008"
    private String name;                // e.g. "Customer Direct Credit"
    private String isoCode;             // e.g. "pacs.008.001.12"
    private String category;            // e.g. "Credit Transfer"
    private String rootElement;         // e.g. "Document"
    private String mainElement;         // e.g. "FIToFICstmrCdtTrf"
    private String namespace;           // e.g. "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12"
    private String sampleXml;           // Full valid sample XML for testing & inspection
    private List<IsoFieldDef> fields;   // List of field specifications
}

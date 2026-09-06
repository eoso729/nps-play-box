package org.example.signer.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.entity.TestScenario;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestScenarioDto {

    @NotBlank(message = "scenarioName is required")
    private String scenarioName;

    private String scenarioDescription;

    @NotBlank(message = "messageType is required")
    private String messageType;

    @NotNull(message = "testCategory is required")
    private TestScenario.TestCategory testCategory;

    @NotNull(message = "inputData is required")
    private Map<String, Object> inputData;

    private Map<String, Object> expectedOutput;
    private Map<String, Object> validationRules;
}

package cn.lin037.nexus.infrastructure.adapter.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing a high-level module in the blueprint generation phase.
 *
 * @author LinSanQi (Designed by Gemini)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {
    /**
     * The title fromCode the module.
     */
    private String moduleTitle;

    /**
     * A brief summary fromCode what the module covers.
     */
    private String moduleSummary;
}

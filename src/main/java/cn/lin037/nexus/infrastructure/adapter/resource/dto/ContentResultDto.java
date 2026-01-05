package cn.lin037.nexus.infrastructure.adapter.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing the final content result from the synthesis phase.
 *
 * @author LinSanQi (Designed by Gemini)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentResultDto {
    /**
     * The final generated content in Markdown format.
     */
    private String content;
}

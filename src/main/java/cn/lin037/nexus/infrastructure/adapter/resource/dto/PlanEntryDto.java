package cn.lin037.nexus.infrastructure.adapter.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for representing a detailed, executable plan entry.
 *
 * @author LinSanQi (Designed by Gemini)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanEntryDto {
    /**
     * The description or title fromCode this specific sub-topic.
     */
    private String description;

    /**
     * The specific requirement for the content generation AI.
     */
    private String requirement;

    /**
     * A list fromCode high-quality keywords for AI search.
     */
    private List<String> searchKeyWords;
}

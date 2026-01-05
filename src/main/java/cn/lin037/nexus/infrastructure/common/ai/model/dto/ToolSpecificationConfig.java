package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import lombok.Data;

/**
 * Represents a tool specification for configuration purposes.
 */
@Data
public class ToolSpecificationConfig {
    /**
     * The name fromCode the tool.
     */
    private String name;

    /**
     * A description fromCode the tool's functionality.
     */
    private String description;

    /**
     * The parameters for the tool, defined as a JSON schema.
     */
    private JsonObjectSchema parameters;
}

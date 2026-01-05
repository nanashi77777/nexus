package cn.lin037.nexus.application.agent.enums;

import lombok.Getter;

@Getter
public enum AcsToolEnum {

    MEMORY_ADD("memory_add", false, true),
    MEMORY_DELETE("memory_delete", false, true),
    LEARNING_PLAN_BATCH_CREATE("learning_plan_batch_create", false, true),
    LEARNING_PLAN_UPDATE("learning_plan_update", false, true),
    LEARNING_PLAN_BATCH_DELETE("learning_plan_batch_delete", false, true),
    LEARNING_PLAN_COMPLETION("learning_plan_completion", false, true),
    RESOURCE_CHUNK_SEARCH("resource_chunk_search", true, false),
    KNOWLEDGE_POINT_SEARCH("knowledge_point_search", true, false),
    SEMANTIC_SEARCH("semantic_search", true, false);

    private final String name;
    private final boolean isReadOperation;
    private final boolean isWriteOperation;

    AcsToolEnum(String name, boolean isReadOperation, boolean isWriteOperation) {
        this.name = name;
        this.isReadOperation = isReadOperation;
        this.isWriteOperation = isWriteOperation;
    }

    public static AcsToolEnum getByName(String name) {
        for (AcsToolEnum value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}

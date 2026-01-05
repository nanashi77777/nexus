package cn.lin037.nexus.infrastructure.adapter.knowledge.constant;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.*;

import java.util.List;

/**
 * 知识生成工作流的Prompt模板与构建器
 * <p>
 * 该类采用流程与Prompt分离的架构，提供三个原子化、无状态的Prompt构建器：
 * TopicExpander（主题扩展）、PointGenerator（知识点生成）、RelationGenerator（关系生成）。
 *
 * @author LinSanQi
 */
public final class KnowledgeAiPrompt {

    private KnowledgeAiPrompt() {
        // 工具类，禁止实例化
    }

    /**
     * Prompt对，包含系统提示和用户提示
     */
    public record PromptPair(String systemPrompt, String userPrompt) {
    }

    /**
     * 生成主题
     */
    public static class TopicExpander {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位 **知识主题规划专家**。你的核心任务是深刻理解【用户核心需求】，并结合【参考资料】，规划出一套能够**完整、系统地**满足用户需求的主题方案。
                
                【核心原则】
                1.  **需求优先**: 所有主题规划都必须围绕【用户核心需求】展开。参考资料是辅助材料，不是唯一依据。
                2.  **全面覆盖**: 确保【用户核心需求】中的每一个要点，以及【所有参考资料】中的每一个`chunk_id`，都被至少一个主题所覆盖。
                3.  **边界清晰**: 每个主题都应有明确的知识边界，避免内容交叉模糊。
                4.  **避免重复**: 避免生成与【已生成的主题】内容上重复的新主题。
                
                【响应字段说明】
                -   `newTopics`: 新生成的主题列表。
                    -   `topicTitle`: 主题标题，简洁明了地概括主题内容。
                    -   `generationRequirement`: 生成要求，详细描述该主题需要涵盖的内容要点，**必须直接回应【用户核心需求】的某个方面**。
                    -   `targetChunkIds`: 该主题关联的参考资料ID列表。如果某个主题是纯粹为了满足用户需求而参考资料中无对应内容，则此字段为空数组 `[]`。
                    -   `estimatedKnowledgePoints`: 预计包含的知识点数量（整数）。
                -   `isComplete`: 布尔值。**表示是否已为【用户核心需求】的所有方面都生成了对应的主题。只有当用户需求被完全覆盖时，才为 `true`，否则为 `false`。这与参考资料是否被完全覆盖无关。**
                -   `newlyCoveredChunkIds`: 本次规划新覆盖的 `chunk_id` 列表。
                
                【工作流程说明】
                你必须严格遵循以下思考步骤：
                1.  **需求解析**: 首先，将【用户核心需求】分解为几个独立的知识要点（例如：“区块链原理”、“共识机制”、“智能合约”、“金融应用”、“供应链应用”）。
                2.  **状态评估**:
                    -   对比【所有参考资料】和【已覆盖的chunkId】，找出尚未被覆盖的 `chunk_id`。
                    -   对比【用户核心需求】的知识要点和【已生成的主题】列表，找出尚未被满足的需求点。
                3.  **主题生成**:
                    -   优先为**尚未被满足的需求点**创建新主题。
                    -   在为需求点创建主题时，从**尚未被覆盖的 `chunk_id`** 中寻找并关联相关内容。
                    -   如果所有需求点都已被主题覆盖，但仍有未覆盖的 `chunk_id`，则为这些剩余的 `chunk_id` 创建补充主题。
                4.  **完成度判断 (最重要)**: 在生成所有新主题后，进行最终检查。将**所有主题（本次新生成的 + 已生成的）**与第一步解析出的**所有知识要点**进行比对。只有当每一个知识要点都有至少一个主题明确对应时，才能将 `isComplete` 设置为 `true`。
                
                【特殊处理规则】
                1.  当【用户核心需求】不为空时，即使没有参考资料，也需要根据用户需求生成相关主题，此时 `targetChunkIds` 和 `newlyCoveredChunkIds` 为空数组 `[]`。
                2.  确保用户需求中的所有内容都被适当的主题覆盖，即使需要创建不关联具体参考资料的主题。
                
                """;

        public static PromptPair build(String userRequirement, List<AiChunkContent> chunks, List<Long> coveredChunkIds, List<TopicDto> existingTopics) {
            StringBuilder userPromptBuilder = new StringBuilder();

            userPromptBuilder.append("【任务描述 - 主题规划】\n");
            userPromptBuilder.append("根据用户需求和参考资料制定主题规划方案。\n\n");

            userPromptBuilder.append("【用户核心需求】\n");
            userPromptBuilder.append(userRequirement).append("\n\n");

            if (coveredChunkIds != null && !coveredChunkIds.isEmpty()) {
                userPromptBuilder.append("【已覆盖的chunkId】\n");
                userPromptBuilder.append("[");
                for (int i = 0; i < coveredChunkIds.size(); i++) {
                    if (i > 0) userPromptBuilder.append(", ");
                    userPromptBuilder.append(coveredChunkIds.get(i));
                }
                userPromptBuilder.append("]\n\n");
            }

            if (existingTopics != null && !existingTopics.isEmpty()) {
                userPromptBuilder.append("【已生成的主题】\n");
                for (TopicDto topic : existingTopics) {
                    userPromptBuilder.append("- ").append(topic.getTopicTitle()).append("\n");
                }
            }
            userPromptBuilder.append("\n");

            userPromptBuilder.append("【参考资料】\n");
            if (chunks != null && !chunks.isEmpty()) {
                for (AiChunkContent chunk : chunks) {
                    userPromptBuilder.append("ID: ").append(chunk.getChunkId()).append("\n");
                    userPromptBuilder.append("内容: ").append(chunk.getChunkContent()).append("\n\n");
                }
            } else {
                userPromptBuilder.append("（暂无参考资料）\n");
            }

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }
    }

    /**
     * 知识点生成器 - 基于子主题生成具体的知识点
     */
    public static class PointGenerator {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位 **资深技术导师** 和 **知识架构师**。你的任务是根据一个**主题批次 (batch)**，并参考**【已生成的知识点清单】**，从上次中断的地方继续生成新的知识点，直到完成整个批次。
                
                【核心原则】
                1.  **规划优先 (Plan First)**: 你的首要任务是进行任务规划，明确还有哪些工作需要完成，**绝对避免重复生成**。
                2.  **严格遵守限制**: 你必须将【生成限制】中的 `maxKnowledgePointsInThisCall` 视为一个**严格的上限**。
                3.  **顺序处理**: 你**必须**严格按照输入批次中的主题顺序，处理待办任务。
                4.  **预算导向**: 在不超过硬顶限制的前提下，将每个主题的 `estimatedKnowledgePoints` 作为生成数量的**指导目标**。
                5.  **避免重复**: 生成的知识点严格不能与【已生成知识点】中的知识点过度拟合重复。
                
                【响应字段说明】
                -   `knowledgePoints`: 一个**扁平化的列表**，包含本次批次中**所有主题**生成的所有知识点。
                    -   `title`: 知识点的核心名词或短语。
                    -   `definition`: 对知识点的精确定义。
                    -   `explanation`: 对知识点的详细讲解。
                    -   `formulaOrCode`: 相关的公式或代码示例。
                    -   `example`: 一个具体的使用示例。
                -   `isComplete`: 布尔值。**表示你是否完成了本次【要生成的主题批次】中的所有任务**。如果因为达到了 `maxKnowledgePointsInThisCall` 的限制而未能处理完批次中的所有主题，此值**必须**为 `false`。
                
                【工作流程与指令优先级】
                1.  **任务规划 (Task Planning)**: 这是你最关键的第一步。
                    a. **识别已完成的主题 (Identify Completed Topics)**: 通过分析【已生成的知识点清单】中的标题，确定【要生成的主题批次】中哪些主题已经被**完全覆盖**。
                    b. **创建待办清单 (Create To-Do List)**: 从原始批次中，创建一个只包含**未完成**主题的新的“待办清单”。
                    c. **检查是否无事可做 (Check for Empty To-Do) - [Escape Hatch]**: 如果这个“待办清单”是空的，意味着所有工作都已完成。你**必须**立即停止，返回一个空的 `knowledgePoints` 数组 `[]` 并将 `isComplete` 设置为 `true`。
                
                2.  **顺序迭代与预算检查**: 从你的“待办清单”的第一个主题开始，严格按顺序执行：
                    a. **预检查**: 判断如果生成当前主题的知识点，是否会导致**总生成数**超过 `maxKnowledgePointsInThisCall`。
                    b. **停止或继续**: 若会超出上限，则**立即停止处理**，不生成当前主题的任何知识点。
                    c. **内容生成**: 结合【用户核心需求】和当前主题的要求，生成新的、不重复的知识点。
                
                3.  **结果合并与完成度判断**:
                    -   将**本次新生成**的所有知识点合并到 `knowledgePoints` 列表中。
                    -   再次检查你的“待办清单”。如果本次调用后，清单中的所有任务都处理完了，则 `isComplete` 为 `true`。如果因为达到了上限而中途停止，导致“待办清单”中仍有未处理的主题，则 `isComplete` 为 `false`。
                
                """;

        public static PromptPair build(String userRequirement, Integer maxKnowledgePointsInThisCall, TopicDto topic, List<String> existingTitles, List<AiChunkContent> chunks) {

            StringBuilder userPromptBuilder = new StringBuilder();

            userPromptBuilder.append("【任务描述 - 知识点生成】\n");
            userPromptBuilder.append("根据用户核心需求、生成限制和下面批次中的主题，生成知识点。\n\n");

            userPromptBuilder.append("【用户核心需求】\n");
            userPromptBuilder.append(userRequirement).append("\n\n");

            userPromptBuilder.append("【生成限制】\n");
            userPromptBuilder.append("**maxKnowledgePointsInThisCall**: ").append(maxKnowledgePointsInThisCall).append("\n\n");

            userPromptBuilder.append("【要生成的主题批次】\n");
            userPromptBuilder.append(JSONUtil.toJsonStr(topic)).append("\n\n");

            userPromptBuilder.append("【已生成的知识点清单】\n");
            if (existingTitles != null && !existingTitles.isEmpty()) {
                for (String title : existingTitles) {
                    userPromptBuilder.append("- ").append(title).append("\n");
                }
            } else {
                userPromptBuilder.append("- （暂无）\n");
            }
            userPromptBuilder.append("\n");

            userPromptBuilder.append("【参考资料】\n");
            if (chunks != null && !chunks.isEmpty()) {
                for (AiChunkContent chunk : chunks) {
                    userPromptBuilder.append("内容: ").append(chunk.getChunkContent()).append("\n\n");
                }
            } else {
                userPromptBuilder.append("（暂无参考资料）\n");
            }

            userPromptBuilder.append("请根据以上信息生成新的知识点，确保不与已存在的知识点重复。返回JSON格式的知识点列表。");

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }
    }

    /**
     * 关系生成器 - 分析知识点之间的关系
     */
    public static class RelationGenerator {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位 **知识图谱构建师** 和 **概念关系分析师**。你的核心任务是深入分析一个【知识点清单】，并参考【已生成的关系清单】，从上次中断的地方继续生成新的、有意义的逻辑关系，直到完成所有分析。
                
                【核心原则】
                1.  **规划优先 (Plan First)**: 你的首要任务是进行任务规划，明确还有哪些知识点对需要分析，**绝对避免重复生成**已存在的关系。
                2.  **严格遵守限制**: 你必须将【生成限制】中的 `maxRelationsInThisCall` 视为一个**严格的上限**。
                3.  **深度分析**: 你必须仔细阅读每个知识点的`title`和`explanation`，理解其核心含义，而不能仅凭标题进行表面判断。
                4.  **关系明确**: 生成的每条关系都必须是清晰、准确且有逻辑支撑的。
                5.  **单向关系**: 优先建立单向关系。除非有明确且不同的反向逻辑，否则不要为同一对知识点生成反向关系。
                
                【关系类型定义】
                你必须从以下预定义的类型中选择关系：
                -   `PRE_REQUISITE`: **前置知识**。学习目标知识点之前，必须先理解源知识点。
                -   `POST_REQUISITE`: **后续知识**。源知识点是学习目标知识点的基础，学习目标知识点是源知识点的延伸或进阶。
                -   `SIMILAR_CONCEPT`: **相似概念**。源知识点和目标知识点在概念上相似，但存在关键区别，适合进行对比学习。
                -   `PART_OF`: **组成部分**。源知识点是构成目标知识点的要素之一。
                -   `APPLICATION_OF`: **应用实例**。目标知识点是源知识点理论或概念的一个具体应用场景或实例。
                -   `EXTENDS`: **扩展关系**。目标知识点是对源知识点的扩展或深化。
                -   `EXAMPLE_OF`: **示例关系**。目标知识点是源知识点的一个具体示例。
                -   `RELATED_TO`: **相关关系**。两个知识点在某些方面相关，但不属于上述任何一种明确关系。
                -   `OPPOSITE_OF`: **对立关系**。两个知识点在概念上是对立或相反的。
                -   `CAUSE_EFFECT`: **因果关系**。源知识点是目标知识点的原因或结果。
                
                【响应字段说明】
                -   `relations`: 一个**扁平化的列表**，包含本次调用中生成的所有新关系。
                    -   `sourceKnowledgeId`: 源知识点ID（必须是知识点清单中存在的ID）。
                    -   `targetKnowledgeId`: 目标知识点ID（必须是知识点清单中存在的ID）。
                    -   `relationType`: 关系类型，必须是【关系类型定义】中列出的一种。
                    -   `relationDescription`: 对该关系的简要、清晰的解释。
                -   `isComplete`: 布尔值。**表示你是否已经分析完【知识点清单】中所有可能的知识点对**。如果因为达到了 `maxRelationsInThisCall` 的限制而未能完成所有分析，此值**必须**为 `false`。
                
                【工作流程与指令优先级】
                1.  **任务规划 (Task Planning)**: 这是你最关键的第一步。
                    a. **构建所有可能的组合**: 在逻辑上，你需要分析的范围是【知识点清单】中所有知识点两两组合的笛卡尔积（排除自身与自身的组合）。
                    b. **识别已完成的工作**: 遍历【已生成的关系清单】，确定哪些知识点对（source-target pair）已经被分析过并建立了关系。
                    c. **创建待办清单 (Create To-Do List)**: 创建一个只包含**未被分析过**的知识点对的“待办清单”。
                    d. **检查是否无事可做 (Check for Empty To-Do) - [Escape Hatch]**: 如果这个“待办清单”是空的，意味着所有工作都已完成。你**必须**立即停止，返回一个空的 `relations` 数组 `[]` 并将 `isComplete` 设置为 `true`。
                
                2.  **顺序迭代与预算检查**: 从你的“待办清单”的第一个知识点对开始，严格按顺序执行：
                    a. **预检查**: 判断如果为当前知识点对生成关系，是否会导致**总生成数**超过 `maxRelationsInThisCall`。
                    b. **停止或继续**: 若会超出上限，则**立即停止处理**，不分析当前知识点对。
                    c. **内容生成**: 分析当前知识点对，如果存在明确的逻辑关系，则生成关系对象。如果不存在，则跳过。
                
                3.  **结果合并与完成度判断**:
                    -   将**本次新生成**的所有关系合并到 `relations` 列表中。
                    -   再次检查你的“待办清单”。如果本次调用后，清单中的所有任务都处理完了，则 `isComplete` 为 `true`。如果因为达到了上限而中途停止，导致“待办清单”中仍有未处理的知识点对，则 `isComplete` 为 `false`。
                """;

        public static PromptPair build(List<AiKnowledgePoint> allPoints, List<AiKnowledgeRelation> existingRelations) {
            StringBuilder userPromptBuilder = new StringBuilder();

            userPromptBuilder.append("【任务描述 - 知识点关系生成】\n");
            userPromptBuilder.append("根据下面的知识点清单，分析并生成它们之间的逻辑关系。\n\n");

            userPromptBuilder.append("【生成限制】\n");
            userPromptBuilder.append("**maxRelationsInThisCall**: 5\n\n");

            userPromptBuilder.append("【严格要求】\n");
            userPromptBuilder.append("每个关系对象必须包含以下完整字段，严禁生成空对象{}：\n");
            userPromptBuilder.append("{\n");
            userPromptBuilder.append("    \"sourceKnowledgeId\": 具体的长整型ID,\n");
            userPromptBuilder.append("    \"targetKnowledgeId\": 具体的长整型ID,\n");
            userPromptBuilder.append("    \"relationType\": \"具体的关系类型字符串\",\n");
            userPromptBuilder.append("    \"relationDescription\": \"具体的关系描述字符串\"\n");
            userPromptBuilder.append("}\n");
            userPromptBuilder.append("如果无法生成完整的关系对象，宁可返回空数组[]。\n\n");

            userPromptBuilder.append("【知识点清单】\n");
            userPromptBuilder.append(JSONUtil.toJsonStr(allPoints)).append("\n\n");

            if (existingRelations != null && !existingRelations.isEmpty()) {
                userPromptBuilder.append("【已生成的关系清单】\n");
                userPromptBuilder.append(JSONUtil.toJsonStr(existingRelations));
            }

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }
    }

    /**
     * 知识点拓展器 - 基于原始知识点进行广度优先拓展
     */
    public static class KnowledgePointExpander {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位 **知识网络分析师 (Knowledge Network Analyst)**。你的核心任务是，围绕一个固定的【原始知识点列表】，并参考【已生成的新知识点】，进行广度优先的拓展，直到原始列表中的每个知识点都得到了关联拓展。
                
                【核心原则】
                1.  **广度优先 (Breadth-First Focus)**: 你的首要目标是确保【原始知识点列表】中的**每一个**知识点都至少被一个或多个新知识点所关联或拓展。在达成这个目标前，避免对新生成的知识点进行再拓展。
                2.  **严格去重 (Strict De-duplication)**: 你生成的新知识点，在核心概念上**绝对不能**与【原始知识点列表】或【已生成的新知识点】中的任何一个重复。
                3.  **优先参考资料 (Prioritize References)**: 新知识点的生成应**优先**从【参考资料(chunkList)】中汲取灵感和依据。如果参考资料不足或未提供，你可以根据【用户核心要求】和你自身的专业知识进行生成。
                4.  **需求导向 (Requirement-Oriented)**: 拓展的方向和内容选择，应始终与【用户核心要求】保持一致。
                5.  **遵守预算 (Budget-Adherent)**: 你必须将【生成限制】中的 `maxNewKnowledgePointsInThisCall` 视为一个**严格的上限**。
                
                【响应字段说明】
                -   `newKnowledgePoints`: 一个扁平化的列表，包含本次调用中生成的所有新知识点。
                    -   `title`, `definition`, `explanation`, `formulaOrCode`, `example`
                -   `isComplete`: 布尔值。**表示你是否认为【原始知识点列表】中的每一个知识点都已被充分拓展（即至少有一个新知识点与其相关联）**。只有当这个条件满足时，此值才为 `true`。
                
                【工作流程与指令优先级】
                1.  **分析覆盖情况 (Coverage Analysis)**:
                    a. **识别未覆盖的原始点**: 遍历【原始知识点列表】。对于其中的每一个"原始点"，检查【已生成的新知识点】中是否存在与其直接相关的新知识点。
                    b. **创建待办清单**: 将所有**尚未被充分拓展**的"原始点"识别出来，形成一个"待办原始点"清单。这些是你的工作焦点。
                    c. **检查是否无事可做 - [Escape Hatch]**: 如果"待办原始点"清单为空（意味着所有原始点都已被拓展），你**必须**立即停止，返回一个空的 `newKnowledgePoints` 数组 `[]` 并将 `isComplete` 设置为 `true`。
                
                2.  **寻找拓展机会**:
                    a. **聚焦待办事项**: 从"待办原始点"清单中选择一个点。
                    b. **扫描参考资料**: **尝试**在【参考资料(chunkList)】中寻找与该"待办原始点"直接相关、且尚未被生成的候选知识主题。
                    c. **确定生成内容**: 如果找到相关参考资料，则基于它来构思新知识点。如果找不到，则基于你对该主题的理解和【用户核心要求】来构思。将所有构思出的候选主题与已有知识点进行最终去重，形成可供生成的"新知识点"列表。
                
                3.  **顺序生成与预算检查**: 从"新知识点"列表的第一个主题开始，严格按顺序执行：
                    a. **预检查**: 判断如果生成当前知识点，是否会导致**总生成数**超过 `maxNewKnowledgePointsInThisCall`。
                    b. **停止或继续**: 若会超出上限，则**立即停止处理**，并将 `isComplete` 设置为 `false`。
                    c. **内容生成**: 生成新的、不重复的知识点。
                
                4.  **完成度判断**:
                    -   在生成新知识点后，重新评估【原始知识点列表】的覆盖情况。如果所有原始点现在都有了关联的新知识点，则将 `isComplete` 设置为 `true`。否则，保持为 `false`。
                
                """;

        public static PromptPair build(String userRequirement, Integer maxNewKnowledgePointsInThisCall,
                                       List<KnowledgePointDto> originalKnowledgePoints,
                                       List<KnowledgePointDto> existingExpandedPoints,
                                       List<AiChunkContent> chunks) {

            StringBuilder userPromptBuilder = new StringBuilder();

            userPromptBuilder.append("【任务描述 - 知识点拓展】\n");
            userPromptBuilder.append("围绕原始知识点列表进行广度优先拓展，确保每个原始知识点都得到充分关联。\n\n");

            userPromptBuilder.append("【用户核心要求】\n");
            userPromptBuilder.append("\"").append(userRequirement).append("\"\n\n");

            userPromptBuilder.append("【生成限制】\n");
            userPromptBuilder.append("{ \"maxNewKnowledgePointsInThisCall\": ").append(maxNewKnowledgePointsInThisCall).append(" }\n\n");

            userPromptBuilder.append("【原始知识点列表】\n");
            JSONUtil.toJsonStr(originalKnowledgePoints);

            userPromptBuilder.append("【已生成的新知识点】\n");
            JSONUtil.toJsonStr(existingExpandedPoints);

            userPromptBuilder.append("【参考资料(chunkList)】\n");
            userPromptBuilder.append("[\n");
            if (chunks != null && !chunks.isEmpty()) {
                for (int i = 0; i < chunks.size(); i++) {
                    AiChunkContent chunk = chunks.get(i);
                    if (i > 0) userPromptBuilder.append(",\n");
                    userPromptBuilder.append("  { \"chunk_id\": \"").append(chunk.getChunkId())
                            .append("\", \"content\": \"").append(chunk.getChunkContent()).append("\" }");
                }
            }
            userPromptBuilder.append("\n]");

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }
    }
}

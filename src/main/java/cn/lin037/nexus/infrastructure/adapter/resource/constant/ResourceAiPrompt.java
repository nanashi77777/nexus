package cn.lin037.nexus.infrastructure.adapter.resource.constant;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.adapter.resource.dto.ContentResultDto;
import cn.lin037.nexus.infrastructure.adapter.resource.dto.ModuleDto;
import cn.lin037.nexus.infrastructure.adapter.resource.dto.PlanEntryDto;

import java.util.List;

/**
 * AI生成资源工作流的Prompt模板与构建器
 * <p>
 * 该类集中管理了“规划-细化-执行”三步流程所需的所有Prompt模板、
 * 格式化逻辑以及用于结构化输出的示例对象。
 * DTOs are defined in the .dto package.
 *
 * @author LinSanQi (Designed by Gemini)
 */
public final class ResourceAiPrompt {

    private ResourceAiPrompt() {
    }

    /**
     * 一个简单的记录类，用于封装构建好的System和User Prompt。
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     */
    public record PromptPair(String systemPrompt, String userPrompt) {
    }

    // =================================================================================================
    // 阶段一: 设计蓝图 (Blueprint Generation)
    // =================================================================================================

    public static final class Blueprint {
        /**
         * 用于结构化输出的示例对象实例。
         */
        public static final ModuleDto EXAMPLE_MODULE = new ModuleDto(
                "示例模块标题",
                "这是一个关于模块核心内容的简要总结。"
        );
        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位 **首席内容战略家 (Chief Content Strategist)**。你的任务是进行高层级的、结构化的头脑风暴，专注于定义一个主题的宏观结构。
                
                【任务描述】
                分析用户提供的【用户请求】，并将其分解为一份逻辑清晰、循序渐进的核心模块列表。每个模块都应代表一个主要的学习或研究领域。
                请专注于整体的广度和逻辑性，暂时不要考虑任何实现细节。你的输出必须是一个JSON数组，每个对象包含 `moduleTitle` 和 `moduleSummary`。
                """;
        private static final String USER_PROMPT_TEMPLATE = """
                【用户请求】
                %s
                """;

        /**
         * 构建用于“设计蓝图”阶段的Prompt。
         *
         * @param userRequest 用户的原始请求字符串。
         * @return 包含System和User Prompt的PromptPair对象。
         */
        public static PromptPair build(String userRequest) {
            String userPrompt = String.format(USER_PROMPT_TEMPLATE, userRequest);
            return new PromptPair(SYSTEM_PROMPT, userPrompt);
        }
    }

    // =================================================================================================
    // 阶段二: 细化模块 (Module Detailing)
    // =================================================================================================

    public static final class Detailing {
        /**
         * 用于结构化输出的示例对象实例。
         */
        public static final PlanEntryDto EXAMPLE_PLAN_ENTRY = new PlanEntryDto(
                "示例子主题",
                "这是一个关于如何编写这个子主题的具体要求。",
                List.of("关键词1", "关键词2", "关键词3")
        );
        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位 **高级研究员 (Senior Researcher)**。你的任务是将一个明确的、高层级的模块，拆解为一系列具体的、可执行的研究点。
                
                【任务描述】
                你将收到【用户的总体目标】以获取全局上下文，以及一个需要你细化的【当前模块】。你的任务是为这个模块生成一份详细的内容规划列表。
                对于列表中的每一个规划条目，你必须提供：
                1. `description`: 一个清晰、简洁的子主题标题。
                2. `requirement`: 一条给内容生成AI的简要指令，概述该子主题需要覆盖的关键点或风格。
                3. `searchKeyWords`: 一个包含3-5个具体的、高质量的关键词列表，供后续的AI搜索代理使用。
                """;
        private static final String USER_PROMPT_TEMPLATE = """
                【用户的总体目标】
                %s
                【所有模块清单】
                %s
                【当前模块】
                %s
                """;

        /**
         * 构建用于“细化模块”阶段的Prompt。
         *
         * @param overallRequest 用户的原始请求字符串。
         * @param moduleToDetail 当前需要细化的模块对象。
         * @return 包含System和User Prompt的PromptPair对象。
         */
        public static PromptPair build(String overallRequest, String allModuleList, ModuleDto moduleToDetail) {
            String moduleJson = JSONUtil.toJsonStr(moduleToDetail);
            String userPrompt = String.format(USER_PROMPT_TEMPLATE, overallRequest, allModuleList, moduleJson);
            return new PromptPair(SYSTEM_PROMPT, userPrompt);
        }
    }

    // =================================================================================================
    // 阶段三: 内容合成 (Content Synthesis)
    // =================================================================================================

    public static final class Synthesis {
        /**
         * 用于结构化输出的示例对象实例。
         * 注意：由于现在是列表输出，示例对象是列表中的单个元素。
         */
        public static final ContentResultDto EXAMPLE_CONTENT_RESULT = new ContentResultDto(
                "这是一个根据搜索结果和具体任务要求生成的**Markdown**格式的知识片段内容。"
        );
        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位 **专家级作者 (Expert Author)**，拥有强大的实时联网搜索和多任务处理能力。
                
                【任务描述】
                你的任务是为【待处理任务列表】中的**每一个**规划条目，分别撰写一篇详细的文章。
                对于列表中的每一个任务，请利用你的搜索能力，并以该任务的`searchKeyWords`作为核心搜索词，进行深入的信息检索。
                然后，综合你检索到的信息，并严格遵循该任务的`requirement`，来撰写最终内容。
                你的写作风格和内容必须严格遵守每个任务各自的指示，同时时刻牢-记【用户的总体目标】，确保所有产出都符合最终愿景。
                你的输出必须是一个JSON数组，数组中的每个对象都只包含一个`content`字段，其内容与输入列表中的任务一一对应。
                """;
        private static final String USER_PROMPT_TEMPLATE = """
                【用户的总体目标】
                %s
                【总任务列表】
                %s
                【待处理任务列表】
                %s
                """;

        /**
         * 构建用于“内容合成”阶段的批量Prompt。
         *
         * @param overallRequest 用户的原始请求字符串。
         * @param planBatch      当前需要批量执行的规划条目列表。
         * @return 包含System和User Prompt的PromptPair对象。
         */
        public static PromptPair build(String overallRequest, String allPlanList, List<PlanEntryDto> planBatch) {
            String planBatchJson = JSONUtil.toJsonStr(planBatch);
            String userPrompt = String.format(USER_PROMPT_TEMPLATE, overallRequest, allPlanList, planBatchJson);
            return new PromptPair(SYSTEM_PROMPT, userPrompt);
        }
    }
}

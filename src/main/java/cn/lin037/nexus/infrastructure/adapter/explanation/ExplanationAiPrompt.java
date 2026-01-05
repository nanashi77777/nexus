package cn.lin037.nexus.infrastructure.adapter.explanation;

import cn.lin037.nexus.infrastructure.adapter.explanation.dto.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 讲解文档AI提示构建器
 *
 * @author LinSanQi
 */
public class ExplanationAiPrompt {

    /**
     * Prompt对，包含系统提示和用户提示
     */
    public record PromptPair(String systemPrompt, String userPrompt) {
    }

    /**
     * 章节规划器
     */
    public static class ChapterPlanner {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位资深的教育家和课程设计师。你的任务是为一位初学者设计一份关于用户指定主题的速成学习文档大纲。
                
                【核心原则】
                1.  **全局视角**: 通读所有用户需求和参考资料，形成一个整体的教学蓝图。
                2.  **逻辑清晰**: 章节顺序必须由浅入深，符合认知规律。
                3.  **资源分配**: 将用户提供的每个`chunk`和`point`合理地分配到最相关的章节中。确保所有资源都被利用。
                4.  **附录处理**: 如果某些资料与核心教学流程关系不大，但仍有价值，请规划一个"附录"章节来收纳它们。
                
                【响应字段说明】
                -   `sectionTitle`: 章节标题，必须简洁明了。
                -   `sectionRequirement`: 对该章节内容的核心教学要求。告诉后续的AI该章节的目标是什么。
                -   `pointIdsForReference`: 关联的用户知识点ID列表。
                -   `chunkIdsForReference`: 关联的用户上传的Chunk ID列表。
                
                【输出要求】
                严格按照JSON格式输出一个包含所有章节规划的数组。不要添加任何解释。
                """;

        public static PromptPair build(String userPrompt,
                                       List<ChunkContentForExplanation> chunks,
                                       List<KnowledgePointForExplanation> knowledgePoints) {
            StringBuilder userPromptBuilder = new StringBuilder();
            userPromptBuilder.append("用户需求：\n").append(userPrompt).append("\n\n");

            userPromptBuilder.append("知识点：\n");
            for (KnowledgePointForExplanation point : knowledgePoints) {
                userPromptBuilder.append("- ").append("{\"pointId\": ").append(point.getPointId())
                        .append(", \"pointTitle\": \"").append(point.getTitle()).append("\"}").append("\n");
            }
            userPromptBuilder.append("\n");

            userPromptBuilder.append("资源片段：\n");
            for (ChunkContentForExplanation chunk : chunks) {
                userPromptBuilder.append("- ").append("{\"chunkId\": ").append(chunk.getChunkId())
                        .append(", \"chunkContent\": \"").append(chunk.getChunkContent()).append("\"}").append("\n");
            }

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }
    }

    /**
     * 小节规划器
     */
    public static class SubSectionPlanner {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位细致的教学内容规划师。你的任务是基于已定好的章节大纲，进一步将其拆解成详细、连贯的小节。
                
                【核心原则】
                1.  **聚焦当前**: 你的任务是为【当前要规划的章节】设计小节，同时可以参考【完整的章节列表】以了解上下文。
                2.  **细化需求**: 为每个小节撰写清晰的`subSectionRequirement`，精确指导内容创作者。
                3.  **资源精确分配**: 将章节关联的`point`和`chunk`，更精确地分配到每个小节。一个资源可被多个小节引用。
                
                【响应字段说明】
                -   `subSectionTitle`: 小节标题。
                -   `subSectionRequirement`: 对该小节内容的核心讲解要求，应包含风格指导（如"使用比喻"）。
                -   `pointIdsForReference`: 关联的用户知识点ID列表。
                -   `chunkIdsForReference`: 关联的用户上传的Chunk ID列表。
                -   `belongSectionId`: 该小节所属的章节ID（由系统在请求时提供）。
                
                【输出要求】
                严格按照JSON格式输出一个包含所有小节规划的数组。
                """;

        public static PromptPair build(ChapterDto chapter,
                                       List<ChunkContentForExplanation> chunks,
                                       List<KnowledgePointForExplanation> knowledgePoints) {
            StringBuilder userPromptBuilder = new StringBuilder();
            userPromptBuilder.append("章节信息：\n");
            userPromptBuilder.append("标题：").append(chapter.getSectionTitle()).append("\n");
            userPromptBuilder.append("教学要求：").append(chapter.getSectionRequirement()).append("\n\n");

            if (chapter.getChunkIdsForReference() != null && !chapter.getChunkIdsForReference().isEmpty()) {
                userPromptBuilder.append("章节关联的资源内容：\n");
                appendChunksPrompt(chunks, userPromptBuilder, chapter.getChunkIdsForReference());
            }

            if (chapter.getPointIdsForReference() != null && !chapter.getPointIdsForReference().isEmpty()) {
                userPromptBuilder.append("章节关联的知识点：\n");
                Map<Long, KnowledgePointForExplanation> pointMap = knowledgePoints.stream()
                        .collect(Collectors.toMap(KnowledgePointForExplanation::getPointId, p -> p));
                for (Long pointId : chapter.getPointIdsForReference()) {
                    KnowledgePointForExplanation point = pointMap.get(pointId);
                    if (point != null) {
                        userPromptBuilder.append("- ").append(point.getTitle()).append("：").append(point.getDefinition()).append("\n");
                    }
                }
                userPromptBuilder.append("\n");
            }

            userPromptBuilder.append("请返回小节列表，每个小节包含：subsectionId（临时ID），parentSectionId（设为").append(chapter.getSectionId()).append("），subsectionTitle，subsectionRequirement，pointIdsForReference，chunkIdsForReference");

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }

        /**
         * 添加资源片段信息
         */
        private static void appendChunksPrompt(List<ChunkContentForExplanation> chunks, StringBuilder userPromptBuilder, List<Long> chunkIdsForReference) {
            Map<Long, ChunkContentForExplanation> chunkMap = chunks.stream()
                    .collect(Collectors.toMap(ChunkContentForExplanation::getChunkId, c -> c));
            for (Long chunkId : chunkIdsForReference) {
                ChunkContentForExplanation chunk = chunkMap.get(chunkId);
                if (chunk != null) {
                    userPromptBuilder.append("- 资源片段ID ").append(chunkId).append("：\n");
                    userPromptBuilder.append(chunk.getChunkContent()).append("\n\n");
                }
            }
        }
    }

    /**
     * 知识点生成器
     */
    public static class KnowledgePointGenerator {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位知识架构师，任务是根据一份详细的文档教学大纲，提炼出其中蕴含的所有核心知识点。
                
                【核心原则】
                1.  **全面覆盖**: 确保为每个小节都生成了足够且相关的知识点。
                2.  **原子性**: 每个知识点应该是一个独立的、可解释的概念。
                3.  **避免重复**: 生成的知识点不能与用户已提供的知识点重复。
                
                【响应字段说明】
                -   `title`: 知识点的核心名词或短语。**这个标题在后续步骤中将被严格引用**。
                -   `definition`: 对知识点的精确定义。
                -   `explanation`: 对知识点的通俗易懂的讲解。
                -   `belongSubSectionId`: 该知识点主要归属的小节ID。
                
                【输出要求】
                严格按照JSON格式输出一个扁平化的知识点列表。
                """;

        public static PromptPair build(String userPrompt,
                                       List<ChapterDto> chapters,
                                       Map<ChapterDto, List<SubSectionDto>> outline,
                                       List<ChunkContentForExplanation> chunks,
                                       List<KnowledgePointForExplanation> existingKnowledgePoints) {
            StringBuilder userPromptBuilder = new StringBuilder();
            userPromptBuilder.append("用户需求：\n").append(userPrompt).append("\n\n");

            userPromptBuilder.append("文档大纲：\n");
            for (ChapterDto chapter : chapters) {
                userPromptBuilder.append("章节：").append(chapter.getSectionTitle()).append("\n");
                List<SubSectionDto> subsections = outline.get(chapter);
                if (subsections != null) {
                    for (SubSectionDto subsection : subsections) {
                        userPromptBuilder.append("  小节：").append(subsection.getSubsectionTitle()).append("\n");
                    }
                }
            }
            // 添加资源片段信息
            appendReference(chunks, userPromptBuilder);

            if (!existingKnowledgePoints.isEmpty()) {
                userPromptBuilder.append("已有知识点（可参考但不要重复）：\n");
                for (KnowledgePointForExplanation point : existingKnowledgePoints) {
                    userPromptBuilder.append("- ").append(point.getTitle()).append("：").append(point.getDefinition()).append("\n");
                }
                userPromptBuilder.append("\n");
            }

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }

        /**
         * 添加资源片段信息
         */
        private static void appendReference(List<ChunkContentForExplanation> chunks, StringBuilder userPromptBuilder) {
            userPromptBuilder.append("\n");

            if (!chunks.isEmpty()) {
                userPromptBuilder.append("可用资源内容：\n");
                for (ChunkContentForExplanation chunk : chunks) {
                    userPromptBuilder.append("- 资源片段ID ").append(chunk.getChunkId()).append("：\n");
                    userPromptBuilder.append(chunk.getChunkContent()).append("\n\n");
                }
            }
        }

        /**
         * 分批生成：基于字符串化大纲和已存在的知识点构建提示词（新增方法）
         */
        public static PromptPair buildWithStringOutlineAndExistingPoints(String userPrompt,
                                                                         String outlineString,
                                                                         List<ChunkContentForExplanation> chunks,
                                                                         List<KnowledgePointForExplanation> existingKnowledgePoints,
                                                                         List<ExplanationPointDto> alreadyGeneratedPoints) {
            StringBuilder userPromptBuilder = new StringBuilder();
            userPromptBuilder.append("用户需求：\n").append(userPrompt).append("\n\n");

            userPromptBuilder.append("当前批次要处理的文档大纲：\n");
            userPromptBuilder.append(outlineString);
            appendReference(chunks, userPromptBuilder);

            // 已有的初始知识点
            if (!existingKnowledgePoints.isEmpty()) {
                userPromptBuilder.append("【重要】已有知识点（绝对不可重复）：\n");
                for (KnowledgePointForExplanation point : existingKnowledgePoints) {
                    userPromptBuilder.append("- ").append(point.getTitle()).append("：").append(point.getDefinition()).append("\n");
                }
                userPromptBuilder.append("\n");
            }

            // 前面批次已生成的知识点
            if (!alreadyGeneratedPoints.isEmpty()) {
                userPromptBuilder.append("【重要】本次任务已生成的知识点（绝对不可重复）：\n");
                for (ExplanationPointDto point : alreadyGeneratedPoints) {
                    userPromptBuilder.append("- ").append(point.getTitle()).append("：").append(point.getDefinition()).append("\n");
                }
                userPromptBuilder.append("\n");
            }

            userPromptBuilder.append("【特别说明】\n");
            userPromptBuilder.append("1. 这是分批处理，请只为当前批次的章节/小节生成知识点\n");
            userPromptBuilder.append("2. 严格避免与上述已有/已生成的知识点重复\n");
            userPromptBuilder.append("3. 如果当前批次的内容与已有知识点高度重叠，可以生成更细化的子概念\n");

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }
    }

    /**
     * 小节内容生成器
     */
    public static class SubSectionContentGenerator {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位充满激情、善用比喻的计算机科学导师。你的任务是为一位初学者撰写一小节引人入胜的教程。
                
                【核心原则】
                1.  **严格遵循教学大纲**: 你的写作内容必须严格围绕【当前小节要求】展开。
                2.  **导师口吻，生动讲解**: 忘掉枯燥的定义！用第一人称"我"或第二人称"你"来拉近与读者的距离。多使用【核心教学风格】中要求的比喻和例子。
                3.  **知识点精确使用 (极其重要)**: 在你的讲解中，当你需要提到【可引用的知识点列表】中的任何概念时，你**必须**使用与"title"完全相同的文字。例如，如果列表里有`"title": "冯·诺依曼体系结构"`，你在正文中就要一字不差地使用"冯·诺依曼体系结构"。
                4.  **结构化输出**: 你的输出必须是一个完整的Markdown字符串。你可以在内容中自由插入**一个或多个**图表或代码块。在所有内容结束后，必须使用`---metadata---`作为分隔符，并在其后提供必需的元数据。
                
                【输出格式与结构】
                [这里是主要讲解内容，使用Markdown格式... ]
                
                你可以在需要的地方插入Mermaid图表，像这样：
                ```mermaid
                graph TD;
                    A --> B;
                ```
                
                [讲解可以继续，你也可以插入代码块...]
                ```java
                public class Example {}
                ```
                
                [所有内容结束后，必须有分隔符和元数据]
                ---metadata---
                Summary: [这里是对本节内容的一句话总结]
                
                【输出示例】
                好的，我们来学习一下什么是“变量”。
                
                想象一下，变量就像是一个贴了标签的盒子。你可以在盒子里放任何东西——数字、文字，甚至是更复杂的东西。比如，我们可以创建一个名为 `age` 的变量，并把数字 `25` 放进去。在程序中，这看起来像这样：
                ```java
                int age = 25;
                ```
                这行代码的意思是：“嘿，计算机！给我一个叫 `age` 的整数盒子，并在里面放上 `25`。”
                
                现在，每当我们需要使用这个人的年龄时，我们只需要说 `age` 就可以了，而不需要每次都重复写 `25`。如果这个人的年龄变了，比如变成 `26` 了，我们只需要更新盒子里的内容：
                ```java
                age = 26;
                ```
                看，就是这么简单！变量让我们的程序变得更加灵活和易于管理。
                
                ---metadata---
                Summary: 变量就像一个贴了标签的盒子，可以存储和管理数据。
                """;

        public static PromptPair build(SubSectionDto subsection,
                                       List<ExplanationPointDto> allPoints,
                                       List<ChunkContentForExplanation> chunks) {
            StringBuilder userPromptBuilder = new StringBuilder();
            userPromptBuilder.append("小节信息：\n");
            userPromptBuilder.append("标题：").append(subsection.getSubsectionTitle()).append("\n");
            userPromptBuilder.append("教学要求：").append(subsection.getSubsectionRequirement()).append("\n\n");

            if (subsection.getPointIdsForReference() != null && !subsection.getPointIdsForReference().isEmpty()) {
                userPromptBuilder.append("相关知识点：\n");
                Map<Long, ExplanationPointDto> pointMap = allPoints.stream()
                        .collect(Collectors.toMap(ExplanationPointDto::getPointId, p -> p));
                for (Long pointId : subsection.getPointIdsForReference()) {
                    ExplanationPointDto point = pointMap.get(pointId);
                    if (point != null) {
                        userPromptBuilder.append("- ").append(point.getTitle()).append("：").append(point.getDefinition()).append("\n");
                        if (point.getFormulaOrCode() != null && !point.getFormulaOrCode().trim().isEmpty()) {
                            userPromptBuilder.append("  公式/代码：").append(point.getFormulaOrCode()).append("\n");
                        }
                        if (point.getExample() != null && !point.getExample().trim().isEmpty()) {
                            userPromptBuilder.append("  示例：").append(point.getExample()).append("\n");
                        }
                    }
                }
                userPromptBuilder.append("\n");
            }

            if (subsection.getChunkIdsForReference() != null && !subsection.getChunkIdsForReference().isEmpty()) {
                userPromptBuilder.append("参考资源内容：\n");
                SubSectionPlanner.appendChunksPrompt(chunks, userPromptBuilder, subsection.getChunkIdsForReference());
            }

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }
    }

    /**
     * 章节内容生成器
     */
    public static class SectionContentGenerator {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位经验丰富的课程编辑。你的任务是为一个章节撰写一段精彩的介绍性引言和一段画龙点睛的总结。
                
                【核心原则】
                1.  **承上启下**: 你的引言需要激发读者的兴趣，告诉他们本章将学到什么，并与前一章（如果有的话）的内容自然衔接。
                2.  **高度概括**: 你的总结需要回顾本章的核心要点，并可以提出一些启发性问题，引导读者思考或展望下一章的内容。
                3.  **精炼语言**: 引言和总结都应言简意赅，不拖泥带水。
                
                【输入解析】
                你将收到本章节的标题和要求，以及本章节下所有小节的标题和内容摘要。请基于这些信息进行创作。
                
                【输出格式与结构】
                [这里是章节的引言部分...]
                
                ---summary---
                
                [这里是章节的总结部分...]
                
                【输出示例】
                欢迎来到“计算机网络基础”这一章！在上一章中，我们探索了计算机的内部世界，了解了CPU、内存和硬盘是如何协同工作的。现在，是时候让这些独立的计算机设备连接起来了！在本章，我们将一起踏上网络之旅，从理解IP地址和DNS如何让我们找到并访问网站，到探索HTTP协议如何让浏览器与服务器进行通信。这些知识将为你打开互联网世界的大门。
                
                ---summary---
                
                在本章中，我们深入浅出地学习了计算机网络的核心概念。我们解释了IP地址就像网络中的门牌号，DNS是帮你查找门牌号的电话簿，而HTTP则是敲开服务器大门并请求信息的礼貌方式。理解这些基本原理，是理解更复杂网络应用（如云计算、网络安全）的基石。现在，思考一下：当你在浏览器中输入一个网址并按下回车后，除了我们提到的步骤，你还知道哪些技术或协议在默默工作呢？在下一章，我们将深入到操作系统内部，看看它是如何管理和调度这些网络资源的。
                """;

        public static PromptPair build(ChapterDto chapter,
                                       List<SubSectionDto> subsections,
                                       List<ExplanationPointDto> allPoints) {
            StringBuilder userPromptBuilder = new StringBuilder();
            userPromptBuilder.append("章节信息：\n");
            userPromptBuilder.append("标题：").append(chapter.getSectionTitle()).append("\n");
            userPromptBuilder.append("教学要求：").append(chapter.getSectionRequirement()).append("\n\n");

            if (subsections != null && !subsections.isEmpty()) {
                userPromptBuilder.append("包含的小节：\n");
                for (SubSectionDto subsection : subsections) {
                    userPromptBuilder.append("- ").append(subsection.getSubsectionTitle()).append("\n");
                }
                userPromptBuilder.append("\n");
            }

            if (chapter.getPointIdsForReference() != null && !chapter.getPointIdsForReference().isEmpty()) {
                userPromptBuilder.append("相关知识点：\n");
                SectionContentBasedOnSubsections.appendKnowledgePointsPrompt(allPoints, userPromptBuilder, chapter.getPointIdsForReference());
                userPromptBuilder.append("\n");
            }

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }
    }

    /**
     * 基于小节内容的章节概述生成器（新增）
     */
    public static class SectionContentBasedOnSubsections {

        private static final String SYSTEM_PROMPT = """
                【AI角色】
                你是一位善于归纳总结的教育专家。你的任务是基于已经生成的小节具体内容，为整个章节撰写引言和总结。
                
                【核心原则】
                1. **基于实际内容**: 必须以小节的实际内容为准，不要凭空想象。
                2. **引言作用**: 为读者预告本章节将要学习的内容，提供学习背景和意义。
                3. **总结作用**: 梳理本章节的关键收获，强化学习成果。
                4. **承上启下**: 如果可能，体现与前后章节的逻辑联系。
                
                【输出要求】
                - 章节开头的引言部分（约200-300字）
                - 章节结尾的总结部分（约150-200字）
                - 整体结构：引言 + 小节内容（已有）+ 总结
                """;

        public static PromptPair build(ChapterOutlineDto chapterOutline,
                                       String subsectionContentSummary,
                                       List<ExplanationPointDto> allPoints) {
            StringBuilder userPromptBuilder = new StringBuilder();

            userPromptBuilder.append("章节标题：").append(chapterOutline.getSectionTitle()).append("\n");
            userPromptBuilder.append("章节要求：").append(chapterOutline.getSectionRequirement()).append("\n\n");

            userPromptBuilder.append("该章节的小节实际内容：\n");
            userPromptBuilder.append(subsectionContentSummary);

            // 添加相关知识点作为参考
            if (chapterOutline.getPointIdsForReference() != null && !chapterOutline.getPointIdsForReference().isEmpty()) {
                userPromptBuilder.append("\n相关知识点：\n");
                appendKnowledgePointsPrompt(allPoints, userPromptBuilder, chapterOutline.getPointIdsForReference());
            }

            return new PromptPair(SYSTEM_PROMPT, userPromptBuilder.toString());
        }

        /**
         * 添加相关知识点作为参考
         */
        private static void appendKnowledgePointsPrompt(List<ExplanationPointDto> allPoints, StringBuilder userPromptBuilder, List<Long> pointIdsForReference) {
            Map<Long, ExplanationPointDto> pointMap = allPoints.stream()
                    .collect(Collectors.toMap(ExplanationPointDto::getPointId, p -> p));
            for (Long pointId : pointIdsForReference) {
                ExplanationPointDto point = pointMap.get(pointId);
                if (point != null) {
                    userPromptBuilder.append("- ").append(point.getTitle()).append("：").append(point.getDefinition()).append("\n");
                }
            }
        }
    }
}
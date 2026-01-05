package cn.lin037.nexus.infrastructure.adapter.explanation.impl;

import cn.lin037.nexus.infrastructure.adapter.explanation.ExplanationAiPrompt;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.*;
import cn.lin037.nexus.infrastructure.adapter.explanation.params.AiGenerateExplanationTaskParameters;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgePoint;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgeRelation;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

/**
 * ExplanationTaskExecutor数据流转测试
 * 不调用真实AI API，通过模拟数据验证各步骤的数据流转是否正确
 *
 * @author LinSanQi
 */
@Slf4j
@SpringBootTest
public class ExplanationTaskExecutorTest {

    private AiGenerateExplanationTaskParameters mockParams;
    private List<ChunkContentForExplanation> mockChunks;
    private List<KnowledgePointForExplanation> mockKnowledgePoints;
    private List<KnowledgeRelationForExplanation> mockKnowledgeRelations;

    @BeforeEach
    void setUp() {
        // 创建模拟的测试数据
        setupMockData();
    }

    /**
     * 测试整个数据流转过程
     */
    @Test
    void testDataFlowProcess() {
        log.info("=== 开始ExplanationTaskExecutor数据流转测试 ===");

        // 步骤1: 测试章节规划
        testChapterPlanning();

        // 步骤2: 测试小节规划
        List<ChapterDto> chapters = createMockChapters();
        Map<ChapterDto, List<SubSectionDto>> outline = testSubSectionPlanning(chapters);

        // 步骤3: 测试ID分配和Map key修复
        Map<Long, List<SubSectionDto>> finalOutline = testIdAssignment(chapters, outline);

        // 步骤4: 测试知识点生成
        List<ExplanationPointDto> explanationPoints = testKnowledgePointGeneration(chapters, finalOutline);

        // 步骤5: 测试关系生成
        testRelationGeneration(explanationPoints);

        // 步骤6: 测试内容生成数据流
        testContentGeneration(chapters, finalOutline, explanationPoints);

        log.info("=== ExplanationTaskExecutor数据流转测试完成 ===");
    }

    /**
     * 步骤1: 测试章节规划数据流
     */
    private void testChapterPlanning() {
        log.info("\n--- 步骤1: 章节规划测试 ---");

        // 模拟构建章节规划提示词
        ExplanationAiPrompt.PromptPair promptPair = ExplanationAiPrompt.ChapterPlanner.build(
                mockParams.getUserPrompt(),
                mockParams.getChunks(),
                mockParams.getKnowledgePoints()
        );

        log.info("章节规划 - 系统提示词长度: {}", promptPair.systemPrompt().length());
        log.info("章节规划 - 用户提示词内容:\n{}", promptPair.userPrompt());

        // 验证提示词是否包含所有必要信息
        String userPrompt = promptPair.userPrompt();
        assert userPrompt.contains("大模型从零基础到可以查看前沿论文");
        assert userPrompt.contains("知识点：");
        assert userPrompt.contains("资源片段：");

        log.info("✅ 章节规划提示词构建正确");
    }

    /**
     * 步骤2: 测试小节规划数据流
     */
    private Map<ChapterDto, List<SubSectionDto>> testSubSectionPlanning(List<ChapterDto> chapters) {
        log.info("\n--- 步骤2: 小节规划测试 ---");

        Map<ChapterDto, List<SubSectionDto>> outline = new HashMap<>();

        for (ChapterDto chapter : chapters) {
            log.info("为章节 '{}' 规划小节", chapter.getSectionTitle());

            // 模拟构建小节规划提示词
            ExplanationAiPrompt.PromptPair promptPair = ExplanationAiPrompt.SubSectionPlanner.build(
                    chapter,
                    mockParams.getChunks(),
                    mockParams.getKnowledgePoints()
            );

            log.info("小节规划 - 章节ID: {}", chapter.getSectionId());
            log.info("小节规划 - 用户提示词内容:\n{}", promptPair.userPrompt());

            // 创建模拟的小节数据
            List<SubSectionDto> subsections = createMockSubSections(chapter);
            outline.put(chapter, subsections);

            log.info("为章节 '{}' 生成了 {} 个小节", chapter.getSectionTitle(), subsections.size());
            for (SubSectionDto subsection : subsections) {
                log.info("  - 小节: {} (临时ID: {})", subsection.getSubsectionTitle(), subsection.getSubsectionId());
            }
        }

        log.info("✅ 小节规划完成，总共生成了 {} 个小节",
                outline.values().stream().mapToInt(List::size).sum());

        return outline;
    }

    /**
     * 步骤3: 测试ID分配和Map key修复
     */
    private Map<Long, List<SubSectionDto>> testIdAssignment(List<ChapterDto> chapters, Map<ChapterDto, List<SubSectionDto>> outline) {
        log.info("\n--- 步骤3: ID分配和Map Key修复测试 ---");

        log.info("ID分配前的数据状态:");
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = outline.get(chapter);
            log.info("章节 '{}' (临时ID: {}) -> {} 个小节",
                    chapter.getSectionTitle(), chapter.getSectionId(),
                    subsections != null ? subsections.size() : 0);

            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    log.info("  - 小节 '{}' (临时ID: {})", subsection.getSubsectionTitle(), subsection.getSubsectionId());
                }
            }
        }

        // 模拟assignPermanentIds方法的逻辑
        Map<Long, List<SubSectionDto>> finalOutline = new HashMap<>();

        for (ChapterDto chapter : chapters) {
            // 先获取对应的小节列表（在修改章节ID之前）
            List<SubSectionDto> subsections = outline.get(chapter);
            Long oldChapterId = chapter.getSectionId();

            // 为章节分配永久ID
            chapter.setSectionId(HutoolSnowflakeIdGenerator.generateLongId());

            // 为该章节下的小节分配永久ID
            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    subsection.setSubsectionId(HutoolSnowflakeIdGenerator.generateLongId());
                    subsection.setParentSectionId(chapter.getSectionId());
                }
                // 使用新的章节永久ID作为key
                finalOutline.put(chapter.getSectionId(), subsections);
            }

            log.info("章节 '{}': 临时ID {} -> 永久ID {}",
                    chapter.getSectionTitle(), oldChapterId, chapter.getSectionId());
        }

        log.info("ID分配后的数据状态:");
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = finalOutline.get(chapter.getSectionId());
            log.info("章节 '{}' (永久ID: {}) -> {} 个小节",
                    chapter.getSectionTitle(), chapter.getSectionId(),
                    subsections != null ? subsections.size() : 0);

            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    log.info("  - 小节 '{}' (永久ID: {}, 父章节ID: {})",
                            subsection.getSubsectionTitle(), subsection.getSubsectionId(), subsection.getParentSectionId());
                }
            }
        }

        // 验证Map key修复是否有效
        log.info("验证Map映射关系:");
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsectionsFromFinalOutline = finalOutline.get(chapter.getSectionId());
            List<SubSectionDto> subsectionsFromOriginalOutline = outline.get(chapter); // 这里应该返回null，因为key已经失效

            log.info("章节 '{}': finalOutline查找结果={}, originalOutline查找结果={}",
                    chapter.getSectionTitle(),
                    subsectionsFromFinalOutline != null ? subsectionsFromFinalOutline.size() + "个小节" : "null",
                    subsectionsFromOriginalOutline != null ? subsectionsFromOriginalOutline.size() + "个小节" : "null"
            );
        }

        log.info("✅ ID分配和Map Key修复完成");
        return finalOutline;
    }

    /**
     * 步骤4: 测试知识点生成数据流
     */
    private List<ExplanationPointDto> testKnowledgePointGeneration(List<ChapterDto> chapters, Map<Long, List<SubSectionDto>> finalOutline) {
        log.info("\n--- 步骤4: 知识点生成测试 ---");

        // 重构outline为兼容格式（模拟ExplanationTaskExecutor中的逻辑）
        Map<ChapterDto, List<SubSectionDto>> outline = new HashMap<>();
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = finalOutline.get(chapter.getSectionId());
            if (subsections != null) {
                outline.put(chapter, subsections);
                log.info("为知识点生成重构映射: 章节 '{}' (ID: {}) -> {} 个小节",
                        chapter.getSectionTitle(), chapter.getSectionId(), subsections.size());
            }
        }

        // 构建知识点生成提示词
        ExplanationAiPrompt.PromptPair promptPair = ExplanationAiPrompt.KnowledgePointGenerator.build(
                mockParams.getUserPrompt(),
                chapters,
                outline,
                mockParams.getChunks(),
                mockParams.getKnowledgePoints()
        );

        log.info("知识点生成 - 用户提示词内容:\n{}", promptPair.userPrompt());

        // 验证提示词是否包含小节信息
        String userPrompt = promptPair.userPrompt();
        assert userPrompt.contains("文档大纲：");
        assert userPrompt.contains("章节：");
        assert userPrompt.contains("小节：");

        // 创建模拟的知识点数据
        List<ExplanationPointDto> explanationPoints = createMockExplanationPoints(finalOutline);

        log.info("生成了 {} 个知识点:", explanationPoints.size());
        for (ExplanationPointDto point : explanationPoints) {
            log.info("  - 知识点: '{}' (ID: {})",
                    point.getTitle(), point.getPointId());
        }

        log.info("✅ 知识点生成测试完成");
        return explanationPoints;
    }

    /**
     * 步骤5: 测试关系生成
     */
    private void testRelationGeneration(List<ExplanationPointDto> explanationPoints) {
        log.info("\n--- 步骤5: 关系生成测试 ---");

        if (explanationPoints.size() < 2) {
            log.info("知识点数量少于2个，跳过关系生成");
            return;
        }

        // 模拟关系生成的映射逻辑
        Map<Long, Long> tempToPermIdMapping = new HashMap<>();
        List<AiKnowledgePoint> tempAiKnowledgePoints = new ArrayList<>();

        for (ExplanationPointDto pointDto : explanationPoints) {
            // 生成临时ID用于关系生成
            Long tempId = HutoolSnowflakeIdGenerator.generateLongId();
            tempToPermIdMapping.put(tempId, pointDto.getPointId());

            AiKnowledgePoint aiPoint = new AiKnowledgePoint(
                    tempId,
                    pointDto.getTitle(),
                    pointDto.getDefinition(),
                    pointDto.getExplanation(),
                    pointDto.getFormulaOrCode(),
                    pointDto.getExample()
            );
            tempAiKnowledgePoints.add(aiPoint);

            log.info("知识点映射: 临时ID {} -> 永久ID {} ({})",
                    tempId, pointDto.getPointId(), pointDto.getTitle());
        }

        // 创建模拟的关系数据
        List<AiKnowledgeRelation> relations = createMockRelations(tempAiKnowledgePoints);

        log.info("生成了 {} 条知识点关系:", relations.size());
        for (AiKnowledgeRelation relation : relations) {
            Long sourcePermanentId = tempToPermIdMapping.get(relation.getSourceKnowledgeId());
            Long targetPermanentId = tempToPermIdMapping.get(relation.getTargetKnowledgeId());
            log.info("  - 关系: {} -> {} (类型: {}, 临时ID: {} -> {})",
                    sourcePermanentId, targetPermanentId, relation.getRelationType(),
                    relation.getSourceKnowledgeId(), relation.getTargetKnowledgeId());
        }

        log.info("✅ 关系生成测试完成");
    }

    /**
     * 步骤6: 测试内容生成数据流
     */
    private void testContentGeneration(List<ChapterDto> chapters, Map<Long, List<SubSectionDto>> finalOutline, List<ExplanationPointDto> allPoints) {
        log.info("\n--- 步骤6: 内容生成测试 ---");

        List<Long> sectionOrderList = new ArrayList<>();
        int totalSubsections = 0;

        for (ChapterDto chapter : chapters) {
            Long sectionId = chapter.getSectionId();
            sectionOrderList.add(sectionId);

            List<SubSectionDto> subsections = finalOutline.get(sectionId);
            List<Long> subsectionOrderList = new ArrayList<>();

            log.info("开始生成章节 '{}' (ID: {}) 的内容", chapter.getSectionTitle(), sectionId);

            // 测试小节内容生成
            if (subsections != null) {
                for (int i = 0; i < subsections.size(); i++) {
                    SubSectionDto subsection = subsections.get(i);
                    Long subsectionId = subsection.getSubsectionId();
                    subsectionOrderList.add(subsectionId);

                    // 构建小节内容生成提示词
                    ExplanationAiPrompt.PromptPair promptPair = ExplanationAiPrompt.SubSectionContentGenerator.build(
                            subsection,
                            allPoints,
                            mockParams.getChunks()
                    );

                    log.info("  小节 '{}' (ID: {}, 顺序: {}) 内容生成提示词长度: {}",
                            subsection.getSubsectionTitle(), subsectionId, i, promptPair.userPrompt().length());

                    // 验证小节内容生成提示词
                    String userPrompt = promptPair.userPrompt();
                    assert userPrompt.contains("小节信息：");
                    assert userPrompt.contains("标题：" + subsection.getSubsectionTitle());

                    totalSubsections++;
                }

                log.info("  章节 '{}' 包含 {} 个小节，小节顺序: {}",
                        chapter.getSectionTitle(), subsections.size(), subsectionOrderList);
            }

            // 测试章节内容生成
            ExplanationAiPrompt.PromptPair chapterPromptPair = ExplanationAiPrompt.SectionContentGenerator.build(
                    chapter,
                    subsections,
                    allPoints
            );

            log.info("  章节 '{}' 内容生成提示词长度: {}",
                    chapter.getSectionTitle(), chapterPromptPair.userPrompt().length());

            // 验证章节内容生成提示词
            String chapterUserPrompt = chapterPromptPair.userPrompt();
            assert chapterUserPrompt.contains("章节信息：");
            assert chapterUserPrompt.contains("标题：" + chapter.getSectionTitle());
        }

        log.info("内容生成数据流测试完成:");
        log.info("  - 生成了 {} 个章节", chapters.size());
        log.info("  - 生成了 {} 个小节", totalSubsections);
        log.info("  - 章节顺序: {}", sectionOrderList);

        log.info("✅ 内容生成测试完成");
    }

    // ========== 模拟数据创建方法 ==========

    private void setupMockData() {
        // 创建模拟的chunk数据
        mockChunks = Arrays.asList(
                createMockChunk(1L, "大模型是一种基于深度学习的人工智能模型，通常包含数十亿到数万亿个参数。"),
                createMockChunk(2L, "Transformer架构是现代大模型的基础，包括编码器和解码器结构。")
        );

        // 创建模拟的知识点数据
        mockKnowledgePoints = Arrays.asList(
                createMockKnowledgePoint(1L, "大模型", "基于深度学习的大规模AI模型"),
                createMockKnowledgePoint(2L, "Transformer", "现代NLP模型的主流架构")
        );

        // 创建模拟的知识关系数据
        mockKnowledgeRelations = Arrays.asList(
                createMockKnowledgeRelation(1L, 2L, "includes", "大模型通常基于Transformer架构")
        );

        // 创建模拟的任务参数
        mockParams = AiGenerateExplanationTaskParameters.builder()
                .explanationDocumentId(1000L)
                .userId(1L)
                .userPrompt("请为我详细地生成一整篇大模型从零基础到可以查看前沿论文程度的学习讲解文档，讲解风格要多运用比喻例子")
                .chunks(mockChunks)
                .knowledgePoints(mockKnowledgePoints)
                .knowledgeRelations(mockKnowledgeRelations)
                .build();
    }

    private List<ChapterDto> createMockChapters() {
        return Arrays.asList(
                ChapterDto.builder()
                        .sectionId(100L) // 临时ID，会被替换
                        .sectionTitle("大模型入门")
                        .sectionRequirement("介绍大模型的基本概念和发展历程，使用通俗易懂的比喻")
                        .pointIdsForReference(Arrays.asList(1L))
                        .chunkIdsForReference(Arrays.asList(1L))
                        .build(),
                ChapterDto.builder()
                        .sectionId(200L) // 临时ID，会被替换
                        .sectionTitle("基础理论")
                        .sectionRequirement("深入讲解大模型的理论基础，包括神经网络和Transformer架构")
                        .pointIdsForReference(Arrays.asList(2L))
                        .chunkIdsForReference(Arrays.asList(2L))
                        .build(),
                ChapterDto.builder()
                        .sectionId(300L) // 临时ID，会被替换
                        .sectionTitle("前沿研究")
                        .sectionRequirement("介绍大模型的最新研究进展和论文解读技巧")
                        .pointIdsForReference(Arrays.asList(1L, 2L))
                        .chunkIdsForReference(Arrays.asList(1L, 2L))
                        .build()
        );
    }

    private List<SubSectionDto> createMockSubSections(ChapterDto chapter) {
        List<SubSectionDto> subsections = new ArrayList<>();

        switch (chapter.getSectionTitle()) {
            case "大模型入门":
                subsections.add(SubSectionDto.builder()
                        .subsectionId(1001L) // 临时ID
                        .subsectionTitle("什么是大模型")
                        .subsectionRequirement("用生活化的比喻解释大模型的概念")
                        .parentSectionId(chapter.getSectionId())
                        .pointIdsForReference(Arrays.asList(1L))
                        .chunkIdsForReference(Arrays.asList(1L))
                        .build());
                subsections.add(SubSectionDto.builder()
                        .subsectionId(1002L) // 临时ID
                        .subsectionTitle("大模型的发展历程")
                        .subsectionRequirement("梳理大模型从起源到现在的发展脉络")
                        .parentSectionId(chapter.getSectionId())
                        .pointIdsForReference(Arrays.asList(1L))
                        .chunkIdsForReference(Arrays.asList(1L))
                        .build());
                break;
            case "基础理论":
                subsections.add(SubSectionDto.builder()
                        .subsectionId(2001L) // 临时ID
                        .subsectionTitle("神经网络基础")
                        .subsectionRequirement("从感知机开始，循序渐进地介绍神经网络")
                        .parentSectionId(chapter.getSectionId())
                        .pointIdsForReference(Arrays.asList(2L))
                        .chunkIdsForReference(Arrays.asList(2L))
                        .build());
                subsections.add(SubSectionDto.builder()
                        .subsectionId(2002L) // 临时ID
                        .subsectionTitle("Transformer架构详解")
                        .subsectionRequirement("深入讲解Transformer的注意力机制和架构设计")
                        .parentSectionId(chapter.getSectionId())
                        .pointIdsForReference(Arrays.asList(2L))
                        .chunkIdsForReference(Arrays.asList(2L))
                        .build());
                break;
            case "前沿研究":
                subsections.add(SubSectionDto.builder()
                        .subsectionId(3001L) // 临时ID
                        .subsectionTitle("如何阅读大模型论文")
                        .subsectionRequirement("提供阅读和理解前沿论文的方法和技巧")
                        .parentSectionId(chapter.getSectionId())
                        .pointIdsForReference(Arrays.asList(1L, 2L))
                        .chunkIdsForReference(Arrays.asList(1L, 2L))
                        .build());
                break;
        }

        return subsections;
    }

    private List<ExplanationPointDto> createMockExplanationPoints(Map<Long, List<SubSectionDto>> finalOutline) {
        List<ExplanationPointDto> points = new ArrayList<>();

        for (List<SubSectionDto> subsections : finalOutline.values()) {
            for (SubSectionDto subsection : subsections) {
                points.add(ExplanationPointDto.builder()
                        .pointId(HutoolSnowflakeIdGenerator.generateLongId())
                        .title("核心概念-" + subsection.getSubsectionTitle())
                        .definition("关于" + subsection.getSubsectionTitle() + "的核心定义")
                        .explanation("详细解释" + subsection.getSubsectionTitle() + "的含义和应用")
                        .formulaOrCode("相关的公式或代码示例")
                        .example("具体的应用示例")
                        .build());
            }
        }

        return points;
    }

    private List<AiKnowledgeRelation> createMockRelations(List<AiKnowledgePoint> points) {
        List<AiKnowledgeRelation> relations = new ArrayList<>();

        if (points.size() >= 2) {
            relations.add(new AiKnowledgeRelation(
                    points.get(0).getId(),
                    points.get(1).getId(),
                    "prerequisite",
                    "第一个知识点是第二个知识点的前置要求"
            ));
        }

        return relations;
    }

    // 辅助方法
    private ChunkContentForExplanation createMockChunk(Long id, String content) {
        return new ChunkContentForExplanation(id, content);
    }

    private KnowledgePointForExplanation createMockKnowledgePoint(Long id, String title, String definition) {
        KnowledgePointForExplanation point = new KnowledgePointForExplanation();
        point.setPointId(id);
        point.setTitle(title);
        point.setDefinition(definition);
        return point;
    }

    private KnowledgeRelationForExplanation createMockKnowledgeRelation(Long sourceId, Long targetId, String relationType, String description) {
        KnowledgeRelationForExplanation relation = new KnowledgeRelationForExplanation();
        relation.setSourcePointId(sourceId);
        relation.setTargetPointId(targetId);
        relation.setRelationType(relationType);
        relation.setDescription(description);
        return relation;
    }
}

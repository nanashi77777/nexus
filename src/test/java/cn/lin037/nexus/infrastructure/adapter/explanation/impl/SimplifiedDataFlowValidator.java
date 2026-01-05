package cn.lin037.nexus.infrastructure.adapter.explanation.impl;

import cn.lin037.nexus.infrastructure.adapter.explanation.ExplanationAiPrompt;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ChapterDto;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ChunkContentForExplanation;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.KnowledgePointForExplanation;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.SubSectionDto;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 简化方案验证器
 * 验证直接修改原Map而不创建新Map的方案
 *
 * @author LinSanQi
 */
@Slf4j
public class SimplifiedDataFlowValidator {

    public static void main(String[] args) {
        System.out.println("=== 简化数据流转方案验证开始 ===");

        SimplifiedDataFlowValidator validator = new SimplifiedDataFlowValidator();
        validator.validateSimplifiedApproach();

        System.out.println("=== 简化方案验证完成 ===");
    }

    /**
     * 验证简化的数据流转方案
     */
    public void validateSimplifiedApproach() {
        log.info("\n--- 简化方案验证 ---");

        // 1. 创建模拟数据
        List<ChapterDto> chapters = createMockChapters();
        System.out.println("创建了 " + chapters.size() + " 个章节");

        // 2. 小节规划，使用ChapterDto作为key
        Map<ChapterDto, List<SubSectionDto>> outline = new HashMap<>();
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = createMockSubSections(chapter);
            outline.put(chapter, subsections);
            System.out.println("章节 '" + chapter.getSectionTitle() + "' (临时ID: " + chapter.getSectionId() + ") -> " + subsections.size() + " 个小节");
        }

        // 3. 简化的ID分配方案 - 直接修改原Map中的对象
        System.out.println("\n开始简化ID分配...");
        assignPermanentIdsSimplified(chapters, outline);

        // 4. 验证修改后Map仍然有效（因为我们没有改变Map的key对象引用）
        System.out.println("\n验证简化方案后的Map查找:");
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = outline.get(chapter);
            System.out.println("章节 '" + chapter.getSectionTitle() + "' (永久ID: " + chapter.getSectionId() + "): 查找结果 = " +
                    (subsections != null ? subsections.size() + "个小节" : "null"));

            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    System.out.println("  - 小节 '" + subsection.getSubsectionTitle() + "' (永久ID: " +
                            subsection.getSubsectionId() + ", 父章节ID: " + subsection.getParentSectionId() + ")");
                }
            }
        }

        // 5. 方案1: 直接使用原Map构建提示词
        System.out.println("\n方案1: 直接使用原Map构建提示词");
        testDirectMapUsage(chapters, outline);

        // 6. 方案2: 转换为字符串传递
        System.out.println("\n方案2: 转换为目录结构字符串");
        testStringBasedApproach(chapters, outline);
    }

    /**
     * 简化的ID分配方法 - 直接修改对象，不改变Map结构
     */
    private void assignPermanentIdsSimplified(List<ChapterDto> chapters, Map<ChapterDto, List<SubSectionDto>> outline) {
        for (ChapterDto chapter : chapters) {
            Long oldChapterId = chapter.getSectionId();

            // 直接修改章节ID
            chapter.setSectionId(HutoolSnowflakeIdGenerator.generateLongId());

            // 获取小节列表并修改ID
            List<SubSectionDto> subsections = outline.get(chapter);
            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    subsection.setSubsectionId(HutoolSnowflakeIdGenerator.generateLongId());
                    subsection.setParentSectionId(chapter.getSectionId());
                }
            }

            log.info("章节 '{}': {} -> {}", chapter.getSectionTitle(), oldChapterId, chapter.getSectionId());
        }

        log.info("✅ 简化ID分配完成，原Map结构保持有效");
    }

    /**
     * 方案1: 直接使用原Map构建提示词
     */
    private void testDirectMapUsage(List<ChapterDto> chapters, Map<ChapterDto, List<SubSectionDto>> outline) {
        // 模拟构建知识点生成提示词
        String userPrompt = "大模型学习文档生成";
        List<ChunkContentForExplanation> chunks = Arrays.asList(
                new ChunkContentForExplanation(1L, "大模型基础知识内容")
        );
        List<KnowledgePointForExplanation> existingPoints = new ArrayList<>();

        ExplanationAiPrompt.PromptPair promptPair = ExplanationAiPrompt.KnowledgePointGenerator.build(
                userPrompt,
                chapters,
                outline, // 直接使用原Map
                chunks,
                existingPoints
        );

        log.info("直接使用原Map的提示词验证:");
        log.info("  - 用户提示词预览:\n{}",
                promptPair.userPrompt().length() > 500 ?
                        promptPair.userPrompt().substring(0, 500) + "..." :
                        promptPair.userPrompt());

        // 验证是否包含小节信息
        String fullPrompt = promptPair.userPrompt();
        boolean hasChapters = fullPrompt.contains("章节：");
        boolean hasSubsections = fullPrompt.contains("小节：");

        log.info("  - 包含章节信息: {}", hasChapters);
        log.info("  - 包含小节信息: {}", hasSubsections);

        if (hasChapters && hasSubsections) {
            log.info("✅ 方案1成功：直接使用原Map构建提示词有效！");
        } else {
            log.error("❌ 方案1失败：直接使用原Map构建提示词无效！");
        }
    }

    /**
     * 方案2: 转换为目录结构字符串
     */
    private void testStringBasedApproach(List<ChapterDto> chapters, Map<ChapterDto, List<SubSectionDto>> outline) {
        // 将outline转换为目录结构字符串
        String outlineString = buildOutlineString(chapters, outline);

        log.info("生成的目录结构字符串:\n{}", outlineString);

        // 模拟在提示词中使用字符串
        String promptTemplate = """
                用户需求：
                大模型学习文档生成
                
                文档大纲：
                %s
                
                请根据以上大纲生成知识点...
                """;

        String finalPrompt = String.format(promptTemplate, outlineString);

        log.info("基于字符串的最终提示词预览:\n{}",
                finalPrompt.length() > 500 ? finalPrompt.substring(0, 500) + "..." : finalPrompt);

        // 验证字符串包含完整信息
        boolean hasChapters = outlineString.contains("章节：");
        boolean hasSubsections = outlineString.contains("  小节：");

        log.info("  - 目录字符串包含章节信息: {}", hasChapters);
        log.info("  - 目录字符串包含小节信息: {}", hasSubsections);

        if (hasChapters && hasSubsections) {
            log.info("✅ 方案2成功：字符串化目录结构有效！");
        } else {
            log.error("❌ 方案2失败：字符串化目录结构无效！");
        }
    }

    /**
     * 将outline转换为目录结构字符串
     */
    private String buildOutlineString(List<ChapterDto> chapters, Map<ChapterDto, List<SubSectionDto>> outline) {
        StringBuilder sb = new StringBuilder();

        for (ChapterDto chapter : chapters) {
            sb.append("章节：").append(chapter.getSectionTitle()).append("\n");

            List<SubSectionDto> subsections = outline.get(chapter);
            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    sb.append("  小节：").append(subsection.getSubsectionTitle()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    // ========== 模拟数据创建方法 ==========

    private List<ChapterDto> createMockChapters() {
        return Arrays.asList(
                ChapterDto.builder()
                        .sectionId(100L) // 临时ID
                        .sectionTitle("大模型入门")
                        .sectionRequirement("介绍大模型的基本概念")
                        .pointIdsForReference(Arrays.asList(1L))
                        .chunkIdsForReference(Arrays.asList(1L))
                        .build(),
                ChapterDto.builder()
                        .sectionId(200L) // 临时ID
                        .sectionTitle("基础理论")
                        .sectionRequirement("深入讲解理论基础")
                        .pointIdsForReference(Arrays.asList(2L))
                        .chunkIdsForReference(Arrays.asList(2L))
                        .build(),
                ChapterDto.builder()
                        .sectionId(300L) // 临时ID
                        .sectionTitle("前沿研究")
                        .sectionRequirement("介绍最新研究进展")
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
                        .subsectionId(1001L)
                        .subsectionTitle("什么是大模型")
                        .subsectionRequirement("用比喻解释大模型概念")
                        .parentSectionId(chapter.getSectionId())
                        .build());
                subsections.add(SubSectionDto.builder()
                        .subsectionId(1002L)
                        .subsectionTitle("大模型发展历程")
                        .subsectionRequirement("梳理发展脉络")
                        .parentSectionId(chapter.getSectionId())
                        .build());
                break;
            case "基础理论":
                subsections.add(SubSectionDto.builder()
                        .subsectionId(2001L)
                        .subsectionTitle("神经网络基础")
                        .subsectionRequirement("介绍神经网络基础")
                        .parentSectionId(chapter.getSectionId())
                        .build());
                subsections.add(SubSectionDto.builder()
                        .subsectionId(2002L)
                        .subsectionTitle("Transformer架构")
                        .subsectionRequirement("详解Transformer")
                        .parentSectionId(chapter.getSectionId())
                        .build());
                break;
            case "前沿研究":
                subsections.add(SubSectionDto.builder()
                        .subsectionId(3001L)
                        .subsectionTitle("论文阅读技巧")
                        .subsectionRequirement("提供论文阅读方法")
                        .parentSectionId(chapter.getSectionId())
                        .build());
                break;
        }

        return subsections;
    }
}

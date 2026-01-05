package cn.lin037.nexus.infrastructure.adapter.explanation.impl;

import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ChapterDto;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.SubSectionDto;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

/**
 * 简化的数据流转验证器
 * Spring Boot测试，可以直接在IDEA中运行验证Map key修复是否有效
 *
 * @author LinSanQi
 */
@Slf4j
@SpringBootTest
public class ExplanationDataFlowValidator {

    @Test
    public void testMapKeyFixValidation() {
        log.info("=== ExplanationTaskExecutor数据流转验证开始 ===");

        validateMapKeyFix();

        log.info("=== 数据流转验证完成 ===");
    }

    @Test
    public void testStringBasedApproach() {
        log.info("=== 方案2: 字符串化方案独立测试开始 ===");

        // 创建测试数据
        List<ChapterDto> chapters = createMockChapters();
        Map<ChapterDto, List<SubSectionDto>> originalOutline = new HashMap<>();

        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = createMockSubSections(chapter);
            originalOutline.put(chapter, subsections);
        }

        // 测试字符串化方案
        testStringBasedDataFlow(chapters, originalOutline);

        log.info("=== 字符串化方案测试完成 ===");
    }

    /**
     * 专门验证Map Key修复问题
     */
    public void validateMapKeyFix() {
        log.info("\n--- Map Key修复验证 ---");

        // 1. 创建模拟数据
        List<ChapterDto> chapters = createMockChapters();
        log.info("创建了 {} 个章节", chapters.size());

        // 2. 模拟小节规划，使用ChapterDto作为key
        Map<ChapterDto, List<SubSectionDto>> originalOutline = new HashMap<>();
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = createMockSubSections(chapter);
            originalOutline.put(chapter, subsections);
            log.info("章节 '{}' (临时ID: {}) -> {} 个小节",
                    chapter.getSectionTitle(), chapter.getSectionId(), subsections.size());
        }

        // 3. 验证修改ID前可以正常获取
        log.info("\n修改ID前的Map查找测试:");
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = originalOutline.get(chapter);
            log.info("章节 '{}': 查找结果 = {}",
                    chapter.getSectionTitle(),
                    subsections != null ? subsections.size() + "个小节" : "null");
        }

        // 4. 模拟assignPermanentIds的逻辑，演示Map key失效问题
        log.info("\n开始模拟ID分配...");
        Map<Long, List<SubSectionDto>> finalOutline = new HashMap<>();
        Map<String, Long> oldToNewIdMapping = new HashMap<>();

        for (ChapterDto chapter : chapters) {
            // 记录旧ID
            Long oldId = chapter.getSectionId();
            String chapterTitle = chapter.getSectionTitle();

            // 先获取小节列表（关键：在修改ID之前）
            List<SubSectionDto> subsections = originalOutline.get(chapter);

            // 修改章节ID（这会导致作为Map key的Chapter对象hashCode变化）
            chapter.setSectionId(HutoolSnowflakeIdGenerator.generateLongId());
            Long newId = chapter.getSectionId();

            oldToNewIdMapping.put(chapterTitle, newId);
            log.info("章节 '{}': {} -> {}", chapterTitle, oldId, newId);

            // 更新小节的父章节ID
            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    subsection.setSubsectionId(HutoolSnowflakeIdGenerator.generateLongId());
                    subsection.setParentSectionId(newId);
                }
                // 使用新ID作为key存储到新Map
                finalOutline.put(newId, subsections);
            }
        }

        // 5. 验证Map key失效问题
        log.info("\n修改ID后的Map查找测试（演示问题）:");
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsectionsFromOriginal = originalOutline.get(chapter);
            List<SubSectionDto> subsectionsFromFinal = finalOutline.get(chapter.getSectionId());

            log.info("章节 '{}' (新ID: {}):", chapter.getSectionTitle(), chapter.getSectionId());
            log.info("  - 从原始Map查找: {}",
                    subsectionsFromOriginal != null ? subsectionsFromOriginal.size() + "个小节" : "null (Key失效!)");
            log.info("  - 从新Map查找: {}",
                    subsectionsFromFinal != null ? subsectionsFromFinal.size() + "个小节" : "null");
        }

        // 6. 测试字符串化方案（方案2）
        log.info("\n测试方案2: 字符串化大纲传递:");
        testStringBasedDataFlow(chapters, originalOutline);

        log.info("\n=== Map Key问题分析总结 ===");
        log.info("问题现象: 修改ChapterDto的sectionId后，从原始Map无法查找到数据（返回null）");
        log.info("问题原因: @Data注解生成的equals()和hashCode()包含sectionId字段，修改后hashCode变化");
        log.info("解决方案: 使用字符串化大纲传递，避免复杂的Map映射关系");
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

    /**
     * 测试字符串化方案的数据流
     */
    private void testStringBasedDataFlow(List<ChapterDto> chapters, Map<ChapterDto, List<SubSectionDto>> originalOutline) {
        log.info("测试字符串化大纲传递方案:");

        // 1. 先保存小节数据到新的Map结构（避免Map Key失效）
        Map<String, List<SubSectionDto>> chapterToSubsections = new HashMap<>();
        for (ChapterDto chapter : chapters) {
            List<SubSectionDto> subsections = originalOutline.get(chapter);
            chapterToSubsections.put(chapter.getSectionTitle(), subsections);
        }

        // 2. 分配永久ID（修改原对象）
        for (ChapterDto chapter : chapters) {
            Long oldId = chapter.getSectionId();
            chapter.setSectionId(HutoolSnowflakeIdGenerator.generateLongId());

            List<SubSectionDto> subsections = chapterToSubsections.get(chapter.getSectionTitle());
            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    subsection.setSubsectionId(HutoolSnowflakeIdGenerator.generateLongId());
                    subsection.setParentSectionId(chapter.getSectionId());
                }
            }

            log.info("章节 '{}': {} -> {}", chapter.getSectionTitle(), oldId, chapter.getSectionId());
        }

        // 3. 生成字符串化大纲（这是方案2的核心）
        String outlineString = buildOutlineStringFixed(chapters, chapterToSubsections);

        log.info("生成的字符串化大纲:\n{}", outlineString);
        log.info("字符串大纲长度: {}", outlineString.length());

        // 4. 验证字符串包含所有必要信息
        boolean allChaptersFound = true;
        boolean allSubsectionsFound = true;

        for (ChapterDto chapter : chapters) {
            if (!outlineString.contains(chapter.getSectionTitle())) {
                log.error("❌ 缺少章节: {}", chapter.getSectionTitle());
                allChaptersFound = false;
            }

            List<SubSectionDto> subsections = chapterToSubsections.get(chapter.getSectionTitle());
            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    if (!outlineString.contains(subsection.getSubsectionTitle())) {
                        log.error("❌ 缺少小节: {}", subsection.getSubsectionTitle());
                        allSubsectionsFound = false;
                    }
                }
            }
        }

        // 5. 模拟在AI提示词中使用字符串化大纲
        String mockUserPrompt = "请为我详细地生成一整篇大模型从零基础到可以查看前沿论文程度的学习讲解文档";
        String aiPromptWithOutline = String.format("""
                用户需求：
                %s
                
                文档大纲：
                %s
                
                可用资源内容：
                - 资源片段ID 1：大模型基础知识内容
                
                请根据以上大纲生成知识点...
                """, mockUserPrompt, outlineString);

        log.info("模拟的AI提示词预览:\n{}",
                aiPromptWithOutline.length() > 600 ?
                        aiPromptWithOutline.substring(0, 600) + "..." :
                        aiPromptWithOutline);

        // 6. 最终验证
        if (allChaptersFound && allSubsectionsFound) {
            log.info("✅ 方案2验证成功：字符串化大纲完整保留了所有结构信息！");
            log.info("   - 优势1: 无需担心Map Key失效问题");
            log.info("   - 优势2: 直接在AI提示词中使用，简洁明了");
            log.info("   - 优势3: 性能更好，避免了复杂的对象映射");
        } else {
            log.error("❌ 方案2验证失败：字符串化大纲信息不完整！");
        }
    }

    /**
     * 构建大纲字符串（修复版本）
     */
    private String buildOutlineStringFixed(List<ChapterDto> chapters, Map<String, List<SubSectionDto>> chapterToSubsections) {
        StringBuilder sb = new StringBuilder();

        for (ChapterDto chapter : chapters) {
            sb.append("章节：").append(chapter.getSectionTitle()).append("\n");

            List<SubSectionDto> subsections = chapterToSubsections.get(chapter.getSectionTitle());
            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    sb.append("  小节：").append(subsection.getSubsectionTitle()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * 构建大纲字符串（原版本 - 已弃用，演示Map Key失效问题）
     */
    private String buildOutlineString(List<ChapterDto> chapters, Map<ChapterDto, List<SubSectionDto>> outline) {
        StringBuilder sb = new StringBuilder();

        for (ChapterDto chapter : chapters) {
            sb.append("章节：").append(chapter.getSectionTitle()).append("\n");

            // 这里会因为Map Key失效而返回null
            List<SubSectionDto> subsections = outline.get(chapter);
            if (subsections != null) {
                for (SubSectionDto subsection : subsections) {
                    sb.append("  小节：").append(subsection.getSubsectionTitle()).append("\n");
                }
            }
        }

        return sb.toString();
    }
}

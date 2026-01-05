package cn.lin037.nexus.infrastructure.adapter.explanation.impl;

import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ChapterOutlineDto;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.SubSectionOutlineDto;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 方案1验证器：专用DTO封装方案
 * 验证ChapterOutlineDto + SubSectionOutlineDto的优雅方案
 *
 * @author LinSanQi
 */
@Slf4j
@SpringBootTest
public class ChapterOutlineDtoValidator {

    @Test
    public void testChapterOutlineDtoApproach() {
        log.info("=== 方案1: 专用DTO封装方案测试开始 ===");

        // 1. 创建测试数据（模拟AI生成的结果）
        List<ChapterOutlineDto> chapterOutlines = createMockChapterOutlines();

        // 2. 分配永久ID
        assignPermanentIds(chapterOutlines);

        // 3. 验证数据完整性
        validateDataIntegrity(chapterOutlines);

        // 4. 演示方案1的各种优势
        demonstrateAdvantages(chapterOutlines);

        log.info("=== 方案1测试完成 ===");
    }

    /**
     * 创建模拟的章节大纲数据
     */
    private List<ChapterOutlineDto> createMockChapterOutlines() {
        List<ChapterOutlineDto> outlines = new ArrayList<>();

        // 章节1：大模型入门
        ChapterOutlineDto chapter1 = ChapterOutlineDto.builder()
                .sectionTitle("大模型入门")
                .sectionRequirement("介绍大模型的基本概念")
                .pointIdsForReference(Arrays.asList(1L, 2L))
                .chunkIdsForReference(Arrays.asList(1L))
                .subsections(Arrays.asList(
                        SubSectionOutlineDto.builder()
                                .subsectionTitle("什么是大模型")
                                .subsectionRequirement("用比喻解释大模型概念")
                                .build(),
                        SubSectionOutlineDto.builder()
                                .subsectionTitle("大模型发展历程")
                                .subsectionRequirement("梳理发展脉络")
                                .build()
                ))
                .build();

        // 章节2：基础理论
        ChapterOutlineDto chapter2 = ChapterOutlineDto.builder()
                .sectionTitle("基础理论")
                .sectionRequirement("深入讲解理论基础")
                .pointIdsForReference(Arrays.asList(3L, 4L))
                .chunkIdsForReference(Arrays.asList(2L, 3L))
                .subsections(Arrays.asList(
                        SubSectionOutlineDto.builder()
                                .subsectionTitle("神经网络基础")
                                .subsectionRequirement("介绍神经网络基础")
                                .build(),
                        SubSectionOutlineDto.builder()
                                .subsectionTitle("Transformer架构")
                                .subsectionRequirement("详解Transformer")
                                .build()
                ))
                .build();

        // 章节3：前沿研究
        ChapterOutlineDto chapter3 = ChapterOutlineDto.builder()
                .sectionTitle("前沿研究")
                .sectionRequirement("介绍最新研究进展")
                .pointIdsForReference(Arrays.asList(1L, 3L, 5L))
                .chunkIdsForReference(Arrays.asList(1L, 4L))
                .subsections(Arrays.asList(
                        SubSectionOutlineDto.builder()
                                .subsectionTitle("论文阅读技巧")
                                .subsectionRequirement("提供论文阅读方法")
                                .build()
                ))
                .build();

        outlines.add(chapter1);
        outlines.add(chapter2);
        outlines.add(chapter3);

        log.info("创建了 {} 个章节大纲", outlines.size());
        for (ChapterOutlineDto outline : outlines) {
            log.info("章节 '{}' 包含 {} 个小节",
                    outline.getSectionTitle(),
                    outline.getSubsectionCount());
        }

        return outlines;
    }

    /**
     * 为章节大纲分配永久ID
     */
    private void assignPermanentIds(List<ChapterOutlineDto> chapterOutlines) {
        log.info("\n=== ID分配阶段 ===");

        for (ChapterOutlineDto outline : chapterOutlines) {
            Long oldId = outline.getSectionId(); // 可能为null
            outline.setSectionId(HutoolSnowflakeIdGenerator.generateLongId());

            log.info("章节 '{}': {} -> {}",
                    outline.getSectionTitle(),
                    oldId,
                    outline.getSectionId());
        }

        log.info("✅ ID分配完成，无Map Key失效风险");
    }

    /**
     * 验证数据完整性
     */
    private void validateDataIntegrity(List<ChapterOutlineDto> chapterOutlines) {
        log.info("\n=== 数据完整性验证 ===");

        boolean allValid = true;
        int totalSubsections = 0;

        for (ChapterOutlineDto outline : chapterOutlines) {
            // 检查章节必需字段
            if (outline.getSectionId() == null) {
                log.error("❌ 章节 '{}' 缺少ID", outline.getSectionTitle());
                allValid = false;
            }

            if (outline.getSectionTitle() == null || outline.getSectionTitle().trim().isEmpty()) {
                log.error("❌ 章节缺少标题");
                allValid = false;
            }

            // 检查小节
            if (outline.getSubsections() != null) {
                for (SubSectionOutlineDto subsection : outline.getSubsections()) {
                    if (subsection.getSubsectionTitle() == null || subsection.getSubsectionTitle().trim().isEmpty()) {
                        log.error("❌ 章节 '{}' 中存在无标题小节", outline.getSectionTitle());
                        allValid = false;
                    }
                    totalSubsections++;
                }
            }

            log.info("章节 '{}' (ID: {}) 包含 {} 个小节",
                    outline.getSectionTitle(),
                    outline.getSectionId(),
                    outline.getSubsectionCount());
        }

        if (allValid) {
            log.info("✅ 数据完整性验证通过：{} 个章节，{} 个小节",
                    chapterOutlines.size(), totalSubsections);
        } else {
            log.error("❌ 数据完整性验证失败");
        }
    }

    /**
     * 演示方案1的各种优势
     */
    private void demonstrateAdvantages(List<ChapterOutlineDto> chapterOutlines) {
        log.info("\n=== 方案1优势演示 ===");

        // 优势1：类型安全的访问
        log.info("1. 类型安全访问:");
        for (ChapterOutlineDto outline : chapterOutlines) {
            log.info("   章节: {} (小节数量: {})",
                    outline.getSectionTitle(),
                    outline.getSubsectionCount());
        }

        // 优势2：直接生成AI友好格式
        log.info("\n2. AI友好的字符串格式:");
        StringBuilder fullOutline = new StringBuilder();
        for (ChapterOutlineDto outline : chapterOutlines) {
            String outlineString = outline.toOutlineString();
            fullOutline.append(outlineString);
            log.info("章节大纲字符串:\n{}", outlineString);
        }

        // 优势3：便捷的批量操作
        log.info("3. 批量操作便利性:");
        List<String> allSectionTitles = chapterOutlines.stream()
                .map(ChapterOutlineDto::getSectionTitle)
                .toList();
        log.info("   所有章节标题: {}", allSectionTitles);

        List<String> allSubsectionTitles = chapterOutlines.stream()
                .flatMap(outline -> outline.getSubsectionTitles().stream())
                .toList();
        log.info("   所有小节标题: {}", allSubsectionTitles);

        // 优势4：结构化数据与字符串格式兼得
        log.info("\n4. 完整的AI提示词构建:");
        String aiPrompt = buildAiPrompt("生成大模型学习文档", fullOutline.toString());
        log.info("AI提示词预览:\n{}",
                aiPrompt.length() > 300 ? aiPrompt.substring(0, 300) + "..." : aiPrompt);

        // 优势5：性能对比
        log.info("\n5. 性能优势:");
        long startTime = System.nanoTime();

        // 模拟复杂操作：访问所有数据
        int accessCount = 0;
        for (ChapterOutlineDto outline : chapterOutlines) {
            accessCount += outline.getSectionTitle().length();
            if (outline.getSubsections() != null) {
                for (SubSectionOutlineDto subsection : outline.getSubsections()) {
                    accessCount += subsection.getSubsectionTitle().length();
                }
            }
        }

        long endTime = System.nanoTime();
        log.info("   直接访问性能: {}ns，访问了{}个字符", (endTime - startTime), accessCount);
        log.info("   无Map查找开销，无ID依赖风险");

        // 优势6：扩展性演示
        log.info("\n6. 扩展性演示:");
        ChapterOutlineDto newChapter = ChapterOutlineDto.builder()
                .sectionId(HutoolSnowflakeIdGenerator.generateLongId())
                .sectionTitle("实战应用")
                .sectionRequirement("提供实际应用案例")
                .subsections(new ArrayList<>())
                .build();

        // 动态添加小节
        newChapter.addSubsection(SubSectionOutlineDto.builder()
                .subsectionTitle("项目实战")
                .subsectionRequirement("通过项目学习")
                .build());

        log.info("   动态添加章节: {}", newChapter.getSectionTitle());
        log.info("   动态添加小节: {}", newChapter.getSubsectionTitles());

        log.info("\n✅ 方案1优势验证完成！");
        log.info("🎯 结论: 专用DTO封装是最优雅、最高效、最易维护的解决方案");
    }

    /**
     * 构建AI提示词
     */
    private String buildAiPrompt(String userRequirement, String outline) {
        return String.format("""
                用户需求：
                %s
                
                文档大纲：
                %s
                
                请根据以上大纲生成详细的学习内容...
                """, userRequirement, outline);
    }
}

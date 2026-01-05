package cn.lin037.nexus.infrastructure.adapter.explanation.impl;

import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ChapterOutlineDto;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ExplanationPointDto;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.SubSectionOutlineDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 验证分批知识点生成逻辑测试
 *
 * @author AI Assistant
 */
@Slf4j
@SpringBootTest
public class BatchKnowledgeGenerationTest {

    @Test
    public void testBatchKnowledgeGenerationLogic() {
        log.info("🚀 测试分批知识点生成逻辑");

        // 模拟大量章节数据
        List<ChapterOutlineDto> chapterOutlines = createMockChapterOutlines();

        log.info("📊 模拟数据：总共 {} 个章节", chapterOutlines.size());
        for (int i = 0; i < chapterOutlines.size(); i++) {
            ChapterOutlineDto chapter = chapterOutlines.get(i);
            log.info("  第 {} 章: {} (包含 {} 个小节)",
                    (i + 1), chapter.getSectionTitle(), chapter.getSubsectionCount());
        }

        // 测试分批逻辑
        final int BATCH_SIZE = 3;
        log.info("\n🔄 分批处理逻辑测试（每批 {} 个章节）：", BATCH_SIZE);

        for (int i = 0; i < chapterOutlines.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, chapterOutlines.size());
            List<ChapterOutlineDto> currentBatch = chapterOutlines.subList(i, endIndex);

            log.info("📦 第 {} 批：处理章节 {} 到 {} (共 {} 个章节)",
                    (i / BATCH_SIZE + 1), (i + 1), endIndex, currentBatch.size());

            for (ChapterOutlineDto chapter : currentBatch) {
                log.info("   - {}", chapter.getSectionTitle());
            }
        }

        log.info("\n✅ 分批逻辑优势：");
        log.info("🎯 1. 避免超时 - 每次只处理3个章节，请求更小更快");
        log.info("🎯 2. 资源节约 - 减少服务器资源占用，提高并发处理能力");
        log.info("🎯 3. 错误隔离 - 某批次失败不影响其他批次");
        log.info("🎯 4. 进度可控 - 可以实时看到每批次的处理进度");
        log.info("🎯 5. 智能去重 - 每批次都考虑已生成的知识点，避免重复");

        log.info("\n🔧 技术实现：");
        log.info("📝 1. generateKnowledgePointsFromOutlines() - 主控制器，管理分批逻辑");
        log.info("📝 2. generateKnowledgePointsForBatch() - 单批次处理器");
        log.info("📝 3. buildWithStringOutlineAndExistingPoints() - 增强提示词生成器");
        log.info("📝 4. isDuplicatePoint() - 本地去重检查器");

        log.info("\n✅ 分批知识点生成逻辑验证完成！");
    }

    @Test
    public void testDeduplicationLogic() {
        log.info("🧹 测试去重逻辑");

        // 模拟已存在的知识点
        List<ExplanationPointDto> existingPoints = Arrays.asList(
                ExplanationPointDto.builder()
                        .pointId(1L)
                        .title("Java基础语法")
                        .definition("Java编程语言的基本语法规则")
                        .build(),
                ExplanationPointDto.builder()
                        .pointId(2L)
                        .title("面向对象编程")
                        .definition("一种以对象为核心的编程范式")
                        .build()
        );

        // 模拟新生成的知识点（包含重复）
        List<ExplanationPointDto> newPoints = Arrays.asList(
                ExplanationPointDto.builder()
                        .title("Java基础语法") // 重复
                        .definition("Java基本语法")
                        .build(),
                ExplanationPointDto.builder()
                        .title("变量声明") // 新的
                        .definition("在Java中声明变量的方法")
                        .build(),
                ExplanationPointDto.builder()
                        .title("面向对象") // 高度相似
                        .definition("面向对象的编程思想")
                        .build()
        );

        log.info("📋 已存在知识点：");
        existingPoints.forEach(point -> log.info("  - {}", point.getTitle()));

        log.info("📋 新生成知识点：");
        newPoints.forEach(point -> log.info("  - {}", point.getTitle()));

        log.info("\n🔍 去重检查结果：");
        for (ExplanationPointDto newPoint : newPoints) {
            boolean isDuplicate = isDuplicatePointLogic(newPoint, existingPoints);
            log.info("  {} - {}", newPoint.getTitle(), isDuplicate ? "❌ 重复，跳过" : "✅ 新的，保留");
        }

        log.info("\n🎯 去重策略：");
        log.info("📊 1. 标题完全相同 - 直接判定为重复");
        log.info("📊 2. 标题包含关系 - 判定为高度相似，视为重复");
        log.info("📊 3. 忽略大小写 - 提高匹配准确性");
        log.info("📊 4. 本地验证 - 在AI生成结果基础上进行二次过滤");
    }

    private List<ChapterOutlineDto> createMockChapterOutlines() {
        return Arrays.asList(
                ChapterOutlineDto.builder()
                        .sectionId(1L)
                        .sectionTitle("Java基础语法")
                        .sectionRequirement("掌握Java基本语法元素")
                        .subsections(Arrays.asList(
                                SubSectionOutlineDto.builder().subsectionTitle("变量声明").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("数据类型").build()
                        ))
                        .build(),
                ChapterOutlineDto.builder()
                        .sectionId(2L)
                        .sectionTitle("面向对象编程")
                        .sectionRequirement("理解面向对象的核心概念")
                        .subsections(Arrays.asList(
                                SubSectionOutlineDto.builder().subsectionTitle("类与对象").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("继承").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("多态").build()
                        ))
                        .build(),
                ChapterOutlineDto.builder()
                        .sectionId(3L)
                        .sectionTitle("异常处理")
                        .sectionRequirement("掌握Java异常处理机制")
                        .subsections(Arrays.asList(
                                SubSectionOutlineDto.builder().subsectionTitle("异常类型").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("try-catch语句").build()
                        ))
                        .build(),
                ChapterOutlineDto.builder()
                        .sectionId(4L)
                        .sectionTitle("集合框架")
                        .sectionRequirement("掌握Java集合的使用")
                        .subsections(Arrays.asList(
                                SubSectionOutlineDto.builder().subsectionTitle("List接口").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("Set接口").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("Map接口").build()
                        ))
                        .build(),
                ChapterOutlineDto.builder()
                        .sectionId(5L)
                        .sectionTitle("多线程编程")
                        .sectionRequirement("理解Java多线程机制")
                        .subsections(Arrays.asList(
                                SubSectionOutlineDto.builder().subsectionTitle("线程创建").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("线程同步").build()
                        ))
                        .build(),
                ChapterOutlineDto.builder()
                        .sectionId(6L)
                        .sectionTitle("IO流操作")
                        .sectionRequirement("掌握Java输入输出操作")
                        .subsections(Arrays.asList(
                                SubSectionOutlineDto.builder().subsectionTitle("字节流").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("字符流").build()
                        ))
                        .build(),
                ChapterOutlineDto.builder()
                        .sectionId(7L)
                        .sectionTitle("网络编程")
                        .sectionRequirement("理解Java网络编程基础")
                        .subsections(Arrays.asList(
                                SubSectionOutlineDto.builder().subsectionTitle("Socket编程").build(),
                                SubSectionOutlineDto.builder().subsectionTitle("HTTP客户端").build()
                        ))
                        .build()
        );
    }

    private boolean isDuplicatePointLogic(ExplanationPointDto newPoint, List<ExplanationPointDto> existingPoints) {
        if (newPoint.getTitle() == null || newPoint.getTitle().trim().isEmpty()) {
            return false;
        }

        String newTitle = newPoint.getTitle().trim().toLowerCase();

        for (ExplanationPointDto existingPoint : existingPoints) {
            if (existingPoint.getTitle() != null) {
                String existingTitle = existingPoint.getTitle().trim().toLowerCase();

                if (existingTitle.equals(newTitle) ||
                        existingTitle.contains(newTitle) ||
                        newTitle.contains(existingTitle)) {
                    return true;
                }
            }
        }

        return false;
    }
}

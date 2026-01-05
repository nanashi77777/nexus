package cn.lin037.nexus.infrastructure.adapter.explanation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 章节大纲DTO - 专门用于大纲传递，避免Map Key失效问题
 * <p>
 * 优势：
 * 1. 结构清晰：一个对象包含完整的章节+小节信息
 * 2. 类型安全：编译时检查，减少运行时错误
 * 3. 易于维护：数据结构直观，便于扩展
 * 4. 避免Map问题：不依赖复杂的Map映射关系
 * 5. AI友好：提供toOutlineString()方法直接生成AI可用格式
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterOutlineDto {

    /**
     * 章节ID（永久ID，分配后不再修改）
     */
    private Long sectionId;

    /**
     * 章节标题
     */
    private String sectionTitle;

    /**
     * 章节要求/描述
     */
    private String sectionRequirement;

    /**
     * 参考知识点ID列表
     */
    private List<Long> pointIdsForReference;

    /**
     * 参考资源块ID列表
     */
    private List<Long> chunkIdsForReference;

    /**
     * 小节列表（核心：直接包含，避免Map映射）
     */
    private List<SubSectionOutlineDto> subsections;

    /**
     * 转换为AI友好的字符串格式
     * 这是方案1相比字符串方案的额外优势：既有结构化数据，又能生成AI格式
     */
    public String toOutlineString() {
        StringBuilder sb = new StringBuilder();
        sb.append("章节：").append(sectionTitle).append("\n");

        if (subsections != null && !subsections.isEmpty()) {
            for (SubSectionOutlineDto subsection : subsections) {
                sb.append("  小节：").append(subsection.getSubsectionTitle()).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 获取所有小节标题列表（便于批量操作）
     */
    public List<String> getSubsectionTitles() {
        if (subsections == null) {
            return List.of();
        }
        return subsections.stream()
                .map(SubSectionOutlineDto::getSubsectionTitle)
                .collect(Collectors.toList());
    }

    /**
     * 获取小节数量
     */
    public int getSubsectionCount() {
        return subsections != null ? subsections.size() : 0;
    }

    /**
     * 添加小节（便于动态构建）
     */
    public void addSubsection(SubSectionOutlineDto subsection) {
        if (this.subsections == null) {
            this.subsections = new java.util.ArrayList<>();
        }
        this.subsections.add(subsection);
    }
}

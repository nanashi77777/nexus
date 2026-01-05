package cn.lin037.nexus.infrastructure.adapter.explanation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小节大纲DTO - 用于大纲传递的简化小节信息
 * <p>
 * 设计理念：
 * 1. 只包含大纲传递必需的字段，避免冗余
 * 2. 不包含ID字段，避免ID变化导致的问题
 * 3. 专门服务于大纲构建和AI提示词生成
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubSectionOutlineDto {

    /**
     * 小节标题
     */
    private String subsectionTitle;

    /**
     * 小节要求/描述
     */
    private String subsectionRequirement;

    /**
     * 从现有的SubSectionDto转换而来（便于兼容现有代码）
     */
    public static SubSectionOutlineDto fromSubSectionDto(SubSectionDto subsectionDto) {
        return SubSectionOutlineDto.builder()
                .subsectionTitle(subsectionDto.getSubsectionTitle())
                .subsectionRequirement(subsectionDto.getSubsectionRequirement())
                .build();
    }

    /**
     * 转换为大纲字符串的一行
     */
    public String toOutlineLine() {
        return "  小节：" + subsectionTitle;
    }
}

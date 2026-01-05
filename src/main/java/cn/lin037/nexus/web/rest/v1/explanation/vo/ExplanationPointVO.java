package cn.lin037.nexus.web.rest.v1.explanation.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 讲解知识点视图对象
 *
 * @author LinSanQi
 */
@Data
public class ExplanationPointVO {

    /**
     * 知识点ID
     */
    private Long id;

    /**
     * 所属讲解文档ID
     */
    private Long explanationDocumentId;

    /**
     * 创建者用户ID
     */
    private Long createdByUserId;

    /**
     * 知识点标题
     */
    private String title;

    /**
     * 知识点定义
     */
    private String definition;

    /**
     * 知识点解释
     */
    private String explanation;

    /**
     * 公式或代码
     */
    private String formulaOrCode;

    /**
     * 使用示例
     */
    private String example;

    /**
     * 节点样式配置
     */
    private Object styleConfig;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

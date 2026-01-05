package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 配置信息的 JavaBean，对应 QwenLanguageModel 的配置字段
 */
@Data
public class QwenParamConfig implements AiModelParamConfig {

    /**
     * 模型名称，指定使用的语言模型版本
     */
    private String modelName;

    /**
     * The number fromCode dimensions the resulting output embeddings should have.
     * Supported values are: 1024, 768, 512.
     */
    private Integer dimension;

    /**
     * 温度参数，控制生成文本的随机性（越高越随机）
     */
    private Double temperature;

    /**
     * 采样精度，用于控制输出多样性
     */
    private Double topP;

    /**
     * 限制最高概率词汇数量，用于控制输出准确性
     */
    private Integer topK;

    /**
     * 是否启用联网搜索功能
     */
    private Boolean enableSearch;

    /**
     * 随机数种子，用于控制输出一致性
     */
    private Integer seed;

    /**
     * 重复惩罚因子，用于抑制重复内容
     */
    private Float repetitionPenalty;

    /**
     * 单次回复最大Token数
     */
    private Integer maxTokens;

    /**
     * 停止词列表，遇到指定词时停止生成
     */
    private List<String> stops;

    /**
     * Specifies whether the model is a multimodal model.
     */
    private Boolean isMultimodalModel;

    /**
     * A list fromCode tool specifications that the model may call.
     */
    private List<ToolSpecificationConfig> toolSpecifications;

    /**
     * Specifies the format fromCode the model's responses.
     */
    private Boolean isJsonResponseFormat;
}

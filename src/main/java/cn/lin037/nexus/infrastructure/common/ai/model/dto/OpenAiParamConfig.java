package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import dev.langchain4j.http.client.HttpClientBuilder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 配置信息的 JavaBean，对应 OpenAiLanguageModel 的配置字段
 */
@Data
public class OpenAiParamConfig implements AiModelParamConfig {

    /**
     * HTTP客户端构建器，用于自定义网络请求相关配置
     */
    private HttpClientBuilder httpClientBuilder;

    /**
     * 组织ID，用于多组织管理时的身份隔离
     */
    private String organizationId;

    /**
     * 项目ID，用于区分不同的项目环境
     */
    private String projectId;

    /**
     * 模型名称，指定使用的语言模型版本
     */
    private String modelName;

    /**
     * The number fromCode dimensions the resulting output embeddings should have.
     * Only supported in {@code text-embedding-3} and newer models.
     */
    private Integer dimensions;

    /**
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     */
    private String user;

    /**
     * 温度参数，控制生成文本的随机性（越高越随机）
     */
    private Double temperature;

    /**
     * Top-p a.k.a. nucleus sampling. Controls diversity via nucleus sampling.
     */
    private Double topP;

    /**
     * The maximum number fromCode tokens to generate in the completion.
     */
    private Integer maxTokens;

    /**
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far,
     * increasing the model's likelihood to talk about new topics.
     */
    private Double presencePenalty;

    /**
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far,
     * decreasing the model's likelihood to repeat the same line verbatim.
     */
    private Double frequencyPenalty;

    /**
     * A list fromCode up to 4 sequences where the API will stop generating further tokens.
     */
    private List<String> stop;

    /**
     * An object specifying the format that the model must output. Setting to { "type": "json_object" } enables JSON mode,
     * which guarantees the message the model generates is valid JSON.
     */
    private String responseFormat;

    /**
     * This feature is in Beta. If specified, our system will make a best effort to sample deterministically,
     * such that repeated requests with the same seed and parameters should return the same result.
     */
    private Integer seed;

    /**
     * A list fromCode tool specifications that the model may call.
     */
    private List<ToolSpecificationConfig> toolSpecifications;

    /**
     * 最大重试次数，在请求失败时自动重试的次数
     */
    private Integer maxRetries;

    /**
     * 是否记录请求日志，用于调试和监控
     */
    private Boolean logRequests;

    /**
     * 是否记录响应日志，用于调试和监控
     */
    private Boolean logResponses;

    /**
     * 自定义请求头，用于添加额外的HTTP头信息
     */
    private Map<String, String> customHeaders;
}

package cn.lin037.nexus.application.resource.port;

import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.List;
import java.util.Map;

/**
 * 向量化端口
 *
 * @author LinSanQi
 */
public interface VectorPort {

    /**
     * 将文本向量化并存储
     *
     * @param text           需要向量化的文本
     * @param metadata       元数据
     * @param embeddingModel 嵌入模型
     * @return 向量ID
     */
    String add(String text, Map<String, String> metadata, EmbeddingModel embeddingModel);

    /**
     * 将文本向量化并存储，返回包含TokenUsage的结果
     *
     * @param text           需要向量化的文本
     * @param metadata       元数据
     * @param embeddingModel 嵌入模型
     * @return 包含向量ID和TokenUsage的结果
     */
    StructResult<String> addWithTokenUsage(String text, Map<String, String> metadata, EmbeddingModel embeddingModel);

    /**
     * 批量将文本向量化并存储
     *
     * @param texts          需要向量化的文本列表
     * @param metadata       元数据
     * @param embeddingModel 嵌入模型
     * @return 向量ID列表
     */
    List<String> batchAdd(List<String> texts, Map<String, String> metadata, EmbeddingModel embeddingModel);

    /**
     * 批量将文本向量化并存储，返回包含TokenUsage的结果
     *
     * @param texts          需要向量化的文本列表
     * @param metadata       元数据
     * @param embeddingModel 嵌入模型
     * @return 包含向量ID列表和TokenUsage的结果
     */
    StructResult<List<String>> batchAddWithTokenUsage(List<String> texts, Map<String, String> metadata, EmbeddingModel embeddingModel);

    /**
     * 将文本向量化并存储
     *
     * @param vectorId       向量ID
     * @param text           需要向量化的文本
     * @param metadata       元数据
     * @param embeddingModel 嵌入模型
     */
    void upsert(String vectorId, String text, Map<String, String> metadata, EmbeddingModel embeddingModel);

    /**
     * 将文本向量化并存储，返回包含TokenUsage的结果
     *
     * @param vectorId       向量ID
     * @param text           需要向量化的文本
     * @param metadata       元数据
     * @param embeddingModel 嵌入模型
     * @return 包含TokenUsage的结果
     */
    StructResult<Void> upsertWithTokenUsage(String vectorId, String text, Map<String, String> metadata, EmbeddingModel embeddingModel);

    /**
     * 根据ID删除向量
     *
     * @param vectorId  向量ID
     * @param dimension 维度
     */
    void delete(String vectorId, Integer dimension);

    /**
     * 根据ID批量删除向量
     *
     * @param vectorIds 向量ID列表
     * @param dimension 维度
     */
    void batchDelete(java.util.List<String> vectorIds, Integer dimension);
} 
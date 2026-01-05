package cn.lin037.nexus.infrastructure.common.ai.service;

import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.VectorizationRequest;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;

import java.util.List;

/**
 * 向量化工具接口
 * 提供文本向量化、存储、更新和检索的核心能力
 *
 * @author LinSanQi
 */
public interface VectorizationTool {

    /**
     * 对单个文本进行向量化并存储
     *
     * @param request        向量化请求，包含文本和元数据
     * @param embeddingModel 用于生成嵌入并确定维度的 EmbeddingModel 实例
     * @return 存储在向量数据库中的唯一ID
     */
    String add(VectorizationRequest request, EmbeddingModel embeddingModel);

    /**
     * 对单个文本进行向量化并存储，返回包含TokenUsage的结果
     *
     * @param request        向量化请求，包含文本和元数据
     * @param embeddingModel 用于生成嵌入并确定维度的 EmbeddingModel 实例
     * @return 包含向量ID和TokenUsage的结果
     */
    StructResult<String> addWithTokenUsage(VectorizationRequest request, EmbeddingModel embeddingModel);

    /**
     * 批量对文本进行向量化并存储
     *
     * @param requests       向量化请求列表
     * @param embeddingModel 用于生成嵌入并确定维度的 EmbeddingModel 实例
     * @return 存储在向量数据库中的ID列表
     */
    List<String> batchAdd(List<VectorizationRequest> requests, EmbeddingModel embeddingModel);

    /**
     * 批量对文本进行向量化并存储，返回包含TokenUsage的结果
     *
     * @param requests       向量化请求列表
     * @param embeddingModel 用于生成嵌入并确定维度的 EmbeddingModel 实例
     * @return 包含向量ID列表和TokenUsage的结果
     */
    StructResult<List<String>> batchAddWithTokenUsage(List<VectorizationRequest> requests, EmbeddingModel embeddingModel);

    /**
     * 更新已存在的向量。如果给定的ID不存在，则执行添加操作。
     *
     * @param id             要更新或添加的向量ID
     * @param request        向量化请求，包含新的文本和元数据
     * @param embeddingModel 用于生成嵌入并确定维度的 EmbeddingModel 实例
     */
    void upsert(String id, VectorizationRequest request, EmbeddingModel embeddingModel);

    /**
     * 更新已存在的向量。如果给定的ID不存在，则执行添加操作，返回包含TokenUsage的结果
     *
     * @param id             要更新或添加的向量ID
     * @param request        向量化请求，包含新的文本和元数据
     * @param embeddingModel 用于生成嵌入并确定维度的 EmbeddingModel 实例
     * @return 包含TokenUsage的结果
     */
    StructResult<Void> upsertWithTokenUsage(String id, VectorizationRequest request, EmbeddingModel embeddingModel);


    /**
     * 根据查询文本，在指定维度的向量表中搜索最相关的文本片段。
     *
     * @param query          查询文本
     * @param embeddingModel 用于生成查询嵌入并确定维度的 EmbeddingModel 实例
     * @param maxResults     返回的最大结果数
     * @param minScore       最小相关性得分
     * @return 搜索结果，包含匹配的文本片段和元数据
     */
    EmbeddingSearchResult<TextSegment> search(String query, EmbeddingModel embeddingModel, int maxResults, double minScore);

    /**
     * 执行一个更复杂的向量搜索请求，可以包含元数据过滤等高级功能。
     *
     * @param searchRequest  嵌入搜索请求对象
     * @param embeddingModel 用于确定维度的 EmbeddingModel 实例
     * @return 搜索结果
     */
    EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest searchRequest, EmbeddingModel embeddingModel);


    /**
     * 根据向量ID删除单个向量
     *
     * @param vectorId  要删除的向量ID
     * @param dimension 向量的维度，用于定位正确的向量表
     */
    void delete(String vectorId, Integer dimension);

    /**
     * 根据向量ID列表批量删除向量
     *
     * @param vectorIds 要删除的向量ID列表
     * @param dimension 向量的维度，用于定位正确的向量表
     */
    void batchDelete(List<String> vectorIds, Integer dimension);
}

package cn.lin037.nexus.infrastructure.common.ai.service.impl;

import cn.lin037.nexus.infrastructure.common.ai.exception.AIInfraExceptionEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.VectorizationRequest;
import cn.lin037.nexus.infrastructure.common.ai.service.VectorizationTool;
import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量化工具实现
 *
 * @author LinSanQi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorizationToolImpl implements VectorizationTool {

    private final Map<Integer, EmbeddingStore<TextSegment>> embeddingStores;

    private EmbeddingStore<TextSegment> getStore(int dimension) {
        EmbeddingStore<TextSegment> store = embeddingStores.get(dimension);
        if (store == null) {
            log.error("未找到支持维度 {} 的向量存储配置。", dimension);
            throw new InfrastructureException(AIInfraExceptionEnum.NO_AVAILABLE_MODEL_FOUND, "未找到支持维度 " + dimension + " 的向量存储");
        }
        return store;
    }

    private EmbeddingStore<TextSegment> getStoreFor(EmbeddingModel embeddingModel) {
        return getStore(embeddingModel.dimension());
    }

    @Override
    public String add(VectorizationRequest request, EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> store = getStoreFor(embeddingModel);
        TextSegment segment = TextSegment.from(request.getText(), new Metadata(request.getMetadata()));
        Response<Embedding> embeddingResponse = embeddingModel.embed(segment);
        return store.add(embeddingResponse.content(), segment);
    }

    @Override
    public List<String> batchAdd(List<VectorizationRequest> requests, EmbeddingModel embeddingModel) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        EmbeddingStore<TextSegment> store = getStoreFor(embeddingModel);
        List<TextSegment> segments = requests.stream()
                .map(req -> TextSegment.from(req.getText(), new Metadata(req.getMetadata())))
                .collect(Collectors.toList());
        Response<List<Embedding>> embeddingResponse = embeddingModel.embedAll(segments);
        return store.addAll(embeddingResponse.content(), segments);
    }

    @Override
    public void upsert(String id, VectorizationRequest request, EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> store = getStoreFor(embeddingModel);
        // LangChain4j 的 upsert 方法本身就是 upsert 逻辑
        TextSegment segment = TextSegment.from(request.getText(), new Metadata(request.getMetadata()));
        Response<Embedding> embeddingResponse = embeddingModel.embed(segment);
        store.add(id, embeddingResponse.content());
        log.info("成功 Upsert 向量: {}", id);
    }

    @Override
    public StructResult<String> addWithTokenUsage(VectorizationRequest request, EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> store = getStoreFor(embeddingModel);
        TextSegment segment = TextSegment.from(request.getText(), new Metadata(request.getMetadata()));
        Response<Embedding> embeddingResponse = embeddingModel.embed(segment);
        String vectorId = store.add(embeddingResponse.content(), segment);
        return StructResult.of(null, vectorId, embeddingResponse.tokenUsage());
    }

    @Override
    public StructResult<List<String>> batchAddWithTokenUsage(List<VectorizationRequest> requests, EmbeddingModel embeddingModel) {
        if (requests == null || requests.isEmpty()) {
            return StructResult.of(null, Collections.emptyList(), null);
        }
        EmbeddingStore<TextSegment> store = getStoreFor(embeddingModel);
        List<TextSegment> segments = requests.stream()
                .map(req -> TextSegment.from(req.getText(), new Metadata(req.getMetadata())))
                .collect(Collectors.toList());
        Response<List<Embedding>> embeddingResponse = embeddingModel.embedAll(segments);
        List<String> vectorIds = store.addAll(embeddingResponse.content(), segments);
        return StructResult.of(null, vectorIds, embeddingResponse.tokenUsage());
    }

    @Override
    public StructResult<Void> upsertWithTokenUsage(String id, VectorizationRequest request, EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> store = getStoreFor(embeddingModel);
        TextSegment segment = TextSegment.from(request.getText(), new Metadata(request.getMetadata()));
        Response<Embedding> embeddingResponse = embeddingModel.embed(segment);
        store.add(id, embeddingResponse.content());
        log.info("成功 Upsert 向量: {}", id);
        return StructResult.of(null, null, embeddingResponse.tokenUsage());
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(String query, EmbeddingModel embeddingModel, int maxResults, double minScore) {
        EmbeddingStore<TextSegment> store = getStoreFor(embeddingModel);
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(query).content())
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        return store.search(searchRequest);
    }

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest searchRequest, EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> store = getStoreFor(embeddingModel);
        return store.search(searchRequest);
    }

    @Override
    public void delete(String vectorId, Integer dimension) {
        EmbeddingStore<TextSegment> embeddingStore = getStore(dimension);
        embeddingStore.remove(vectorId);
    }

    @Override
    public void batchDelete(List<String> vectorIds, Integer dimension) {
        EmbeddingStore<TextSegment> embeddingStore = getStore(dimension);
        embeddingStore.removeAll(vectorIds);
    }
} 
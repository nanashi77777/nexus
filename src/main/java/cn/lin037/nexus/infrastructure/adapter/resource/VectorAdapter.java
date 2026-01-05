package cn.lin037.nexus.infrastructure.adapter.resource;

import cn.lin037.nexus.application.resource.port.VectorPort;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.VectorizationRequest;
import cn.lin037.nexus.infrastructure.common.ai.service.VectorizationTool;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 向量端口适配器
 *
 * @author LinSanQi
 */
@Component
@RequiredArgsConstructor
public class VectorAdapter implements VectorPort {

    private final VectorizationTool vectorizationTool;

    @Override
    public String add(String text, Map<String, String> metadata, EmbeddingModel embeddingModel) {
        VectorizationRequest request = new VectorizationRequest(text, metadata);
        return vectorizationTool.add(request, embeddingModel);
    }

    @Override
    public StructResult<String> addWithTokenUsage(String text, Map<String, String> metadata, EmbeddingModel embeddingModel) {
        VectorizationRequest request = new VectorizationRequest(text, metadata);
        return vectorizationTool.addWithTokenUsage(request, embeddingModel);
    }

    @Override
    public List<String> batchAdd(List<String> texts, Map<String, String> metadata, EmbeddingModel embeddingModel) {
        List<VectorizationRequest> requests = texts.stream()
                .map(text -> new VectorizationRequest(text, metadata))
                .toList();
        return vectorizationTool.batchAdd(requests, embeddingModel);
    }

    @Override
    public StructResult<List<String>> batchAddWithTokenUsage(List<String> texts, Map<String, String> metadata, EmbeddingModel embeddingModel) {
        List<VectorizationRequest> requests = texts.stream()
                .map(text -> new VectorizationRequest(text, metadata))
                .toList();
        return vectorizationTool.batchAddWithTokenUsage(requests, embeddingModel);
    }

    @Override
    public void upsert(String vectorId, String text, Map<String, String> metadata, EmbeddingModel embeddingModel) {
        VectorizationRequest request = new VectorizationRequest(text, metadata);
        vectorizationTool.upsert(vectorId, request, embeddingModel);
    }

    @Override
    public StructResult<Void> upsertWithTokenUsage(String vectorId, String text, Map<String, String> metadata, EmbeddingModel embeddingModel) {
        VectorizationRequest request = new VectorizationRequest(text, metadata);
        return vectorizationTool.upsertWithTokenUsage(vectorId, request, embeddingModel);
    }

    @Override
    public void delete(String vectorId, Integer dimension) {
        vectorizationTool.delete(vectorId, dimension);
    }

    @Override
    public void batchDelete(List<String> vectorIds, Integer dimension) {
        vectorizationTool.batchDelete(vectorIds, dimension);
    }
} 
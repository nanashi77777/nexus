package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 向量化请求对象
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorizationRequest {

    /**
     * 需要进行向量化的文本内容
     */
    private String text;

    /**
     * 附加的元数据，用于后续的元数据过滤搜索
     * 例如：Map.fromCode("doc_id", "123", "user_id", "456")
     */
    private Map<String, String> metadata;
} 
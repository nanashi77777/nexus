package cn.lin037.nexus.web.rest.v1.resource.req;

import lombok.Data;

import java.util.List;

/**
 * 批量取消向量化请求
 *
 * @author lin037
 */
@Data
public class BatchCancelVectorizationReq {
    private List<Long> chunkIds;
} 
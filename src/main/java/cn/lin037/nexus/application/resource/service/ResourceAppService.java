package cn.lin037.nexus.application.resource.service;

import cn.lin037.nexus.application.resource.vo.ResourceCreatedVO;
import cn.lin037.nexus.web.rest.v1.resource.req.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资源应用服务接口
 *
 * @author LinSanQi
 */
public interface ResourceAppService {

    /**
     * 手动创建资源
     *
     * @param request 创建请求
     * @return 资源ID
     */
    Long createManualResource(CreateManualResourceReq request);

    /**
     * 上传文件并创建资源
     *
     * @param request 创建请求
     * @param file    上传的文件
     * @return 资源ID
     */
    ResourceCreatedVO uploadResourceFile(CreateManualResourceReq request, MultipartFile file);

    /**
     * 从链接创建资源
     *
     * @param request 创建请求
     * @return 资源ID
     */
    ResourceCreatedVO createResourceFromLink(CreateLinkResourceReq request);

    /**
     * 从AI搜索创建资源
     *
     * @param request 创建请求
     * @return 创建的资源ID
     */
    ResourceCreatedVO createAiSearchResource(CreateAiSearchResourceReq request);

    /**
     * 更新资源信息
     *
     * @param resourceId 资源ID
     * @param request    更新请求
     */
    void updateResource(Long resourceId, UpdateResourceReq request);

    /**
     * 删除资源
     *
     * @param resourceId 资源ID
     */
    void deleteResource(Long resourceId);

    /**
     * 为指定资源创建分片
     *
     * @param resourceId 资源ID
     * @param request    创建分片请求
     * @return 新建的分片ID
     */
    Long createResourceChunk(Long resourceId, CreateChunkReq request);

    /**
     * 更新资源分片内容
     *
     * @param chunkId 分片ID
     * @param request 更新请求
     */
    void updateResourceChunk(Long chunkId, UpdateChunkReq request);

    /**
     * 触发分片向量化
     *
     * @param chunkId 分片ID
     * @return 向量化任务ID
     */
    Long triggerChunkVectorization(Long chunkId);

    /**
     * 触发分片批量向量化
     *
     * @param request 包含分片ID列表的请求
     * @return 批量向量化任务ID
     */
    Long triggerBatchChunkVectorization(BatchVectorizeReq request);

    /**
     * 删除资源分片
     *
     * @param chunkId 分片ID
     */
    void deleteResourceChunk(Long chunkId);

    /**
     * 取消分片向量化
     *
     * @param chunkId 分片ID
     */
    void cancelChunkVectorization(Long chunkId);

    /**
     * 批量取消分片向量化
     *
     * @param request 包含分片ID列表的请求
     */
    void cancelBatchChunkVectorization(BatchCancelVectorizationReq request);
}

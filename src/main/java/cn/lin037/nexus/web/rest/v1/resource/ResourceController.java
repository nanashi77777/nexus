package cn.lin037.nexus.web.rest.v1.resource;

import cn.lin037.nexus.application.resource.service.ResourceAppService;
import cn.lin037.nexus.application.resource.vo.ResourceCreatedVO;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.resource.req.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资源管理接口
 *
 * @author LinSanQi
 */
@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    private final ResourceAppService resourceAppService;

    public ResourceController(ResourceAppService resourceAppService) {
        this.resourceAppService = resourceAppService;
    }

    // ====== Write Operations ======

    /**
     * 创建手动资源
     * COMPLETED
     *
     * @param request 创建手动资源请求
     * @return 新创建的资源ID
     */
    @PostMapping("/manual")
    public ResultVO<Long> createManualResource(@Valid @RequestBody CreateManualResourceReq request) {
        Long resourceId = resourceAppService.createManualResource(request);
        return ResultVO.success(resourceId);
    }

    /**
     * 上传资源文件
     * COMPLETED
     *
     * @param file 文件
     * @return 新创建的资源ID
     */
    @PostMapping("/upload")
    public ResultVO<ResourceCreatedVO> uploadResourceFile(
            @Valid @RequestPart("request") CreateManualResourceReq request,
            @RequestPart("file") MultipartFile file) {
        ResourceCreatedVO createdVO = resourceAppService.uploadResourceFile(request, file);
        return ResultVO.success(createdVO);
    }

    /**
     * 创建链接资源
     * COMPLETED
     *
     * @param request 创建链接资源请求
     * @return 新创建的资源ID
     */
    @PostMapping("/link")
    public ResultVO<ResourceCreatedVO> createResourceFromLink(@Valid @RequestBody CreateLinkResourceReq request) {
        ResourceCreatedVO createdVO = resourceAppService.createResourceFromLink(request);
        return ResultVO.success(createdVO);
    }

    /**
     * 创建AI搜索资源
     * COMPLETED
     *
     * @param request 创建AI搜索资源请求
     * @return 新创建的资源ID
     */
    @PostMapping("/ai-search")
    public ResultVO<ResourceCreatedVO> createResourceFromAiSearch(@Valid @RequestBody CreateAiSearchResourceReq request) {
        ResourceCreatedVO createdVO = resourceAppService.createAiSearchResource(request);
        return ResultVO.success(createdVO);
    }

    /**
     * 更新资源信息
     * COMPLETED
     *
     * @param resourceId 资源ID
     * @param request    更新资源请求
     * @return 无
     */
    @PutMapping("/{resourceId}")
    public ResultVO<Void> updateResource(@PathVariable Long resourceId, @Valid @RequestBody UpdateResourceReq request) {
        resourceAppService.updateResource(resourceId, request);
        return ResultVO.success();
    }

    /**
     * 为资源创建新的分片
     * COMPLETED
     *
     * @param resourceId 资源ID
     * @param request    创建分片请求
     * @return 新创建的分片ID
     */
    @PostMapping("/{resourceId}/chunks")
    public ResultVO<Long> createChunk(@PathVariable Long resourceId, @Valid @RequestBody CreateChunkReq request) {
        Long chunkId = resourceAppService.createResourceChunk(resourceId, request);
        return ResultVO.success(chunkId);
    }

    /**
     * 更新资源块
     * COMPLETED
     *
     * @param chunkId 资源块ID
     * @param request 更新资源块请求
     * @return 无
     */
    @PutMapping("/chunks/{chunkId}")
    public ResultVO<Void> updateChunk(@PathVariable Long chunkId, @Valid @RequestBody UpdateChunkReq request) {
        resourceAppService.updateResourceChunk(chunkId, request);
        return ResultVO.success();
    }

    /**
     * 触发向量化
     * COMPLETED
     *
     * @param chunkId 块ID
     * @return 无
     */
    @PostMapping("/chunks/{chunkId}/vectorize")
    public ResultVO<Long> vectorizeChunk(@PathVariable Long chunkId) {
        return ResultVO.success(resourceAppService.triggerChunkVectorization(chunkId));
    }

    /**
     * 删除资源分片
     * COMPLETED
     *
     * @param chunkId 块ID
     * @return 无
     */
    @DeleteMapping("/chunks/{chunkId}")
    public ResultVO<Void> deleteChunk(@PathVariable Long chunkId) {
        resourceAppService.deleteResourceChunk(chunkId);
        return ResultVO.success();
    }

    /**
     * 取消分片向量化
     * COMPLETED
     *
     * @param chunkId 块ID
     * @return 无
     */
    @DeleteMapping("/chunks/{chunkId}/vectorization")
    public ResultVO<Void> cancelVectorization(@PathVariable Long chunkId) {
        resourceAppService.cancelChunkVectorization(chunkId);
        return ResultVO.success();
    }

    /**
     * 触发分片批量向量化
     *
     * @param request 包含分片ID列表的请求
     * @return 批量向量化任务ID
     */
    @PostMapping("/chunks/batch-vectorize")
    public ResultVO<Long> vectorizeChunks(@Valid @RequestBody BatchVectorizeReq request) {
        Long taskId = resourceAppService.triggerBatchChunkVectorization(request);
        return ResultVO.success(taskId);
    }

    /**
     * 批量取消分片向量化
     *
     * @param request 包含分片ID列表的请求
     * @return 无
     */
    @DeleteMapping("/chunks/batch-vectorization")
    public ResultVO<Void> cancelBatchVectorization(@Valid @RequestBody BatchCancelVectorizationReq request) {
        resourceAppService.cancelBatchChunkVectorization(request);
        return ResultVO.success();
    }
} 
package cn.lin037.nexus.application.resource.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.common.port.FilePort;
import cn.lin037.nexus.application.isolation.port.LearningSpaceRepository;
import cn.lin037.nexus.application.resource.enums.ResourceErrorCodeEnum;
import cn.lin037.nexus.application.resource.enums.ResourceSourceTypeEnum;
import cn.lin037.nexus.application.resource.enums.ResourceStatusEnum;
import cn.lin037.nexus.application.resource.enums.SliceStrategyEnum;
import cn.lin037.nexus.application.resource.port.ResourceChunkRepository;
import cn.lin037.nexus.application.resource.port.ResourceRepository;
import cn.lin037.nexus.application.resource.port.ResourceTaskPort;
import cn.lin037.nexus.application.resource.port.VectorPort;
import cn.lin037.nexus.application.resource.service.ResourceAppService;
import cn.lin037.nexus.application.resource.vo.ResourceCreatedVO;
import cn.lin037.nexus.common.constant.enums.result.impl.CommonResultCodeEnum;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.file.enums.AccessLevel;
import cn.lin037.nexus.infrastructure.common.file.model.InfraFileMetadata;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceEntity;
import cn.lin037.nexus.web.rest.v1.resource.req.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源应用服务实现
 *
 * @author LinSanQi
 */
@Service
public class ResourceAppServiceImpl implements ResourceAppService {

    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of("ppt", "pptx", "doc", "docx", "md", "txt", "pdf");
    private final ResourceRepository resourceRepository;
    private final ResourceChunkRepository resourceChunkRepository;
    private final LearningSpaceRepository learningSpaceRepository;
    private final FilePort filePort;
    private final ResourceTaskPort resourceTaskPort;
    private final VectorPort vectorPort;

    public ResourceAppServiceImpl(ResourceRepository resourceRepository,
                                  ResourceChunkRepository resourceChunkRepository,
                                  LearningSpaceRepository learningSpaceRepository,
                                  FilePort filePort,
                                  ResourceTaskPort resourceTaskPort, VectorPort vectorPort) {
        this.resourceRepository = resourceRepository;
        this.resourceChunkRepository = resourceChunkRepository;
        this.learningSpaceRepository = learningSpaceRepository;
        this.filePort = filePort;
        this.resourceTaskPort = resourceTaskPort;
        this.vectorPort = vectorPort;
    }

    /**
     * 创建手动上传资源
     *
     * @param request 创建资源请求
     * @return 新资源ID
     */
    @Override
    public Long createManualResource(CreateManualResourceReq request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long learningSpaceId = request.getLearningSpaceId();

        // 验证学习空间并创建新资源（其会设置ResourceStatusEnum.PENDING_PARSE.getCode()）
        ResourceEntity newResource = validateAndCreateResource(
                learningSpaceId,
                userId,
                request.getTitle(),
                request.getDescription(),
                ResourceSourceTypeEnum.MANUAL.getCode(),
                null);

        // 保存资源
        resourceRepository.save(newResource);

        // 返回资源ID
        return newResource.getRsId();
    }

    /**
     * 从文件创建资源
     *
     * @param request 创建资源请求
     * @param file    资源文件
     * @return 新资源ID
     */
    @Override
    public ResourceCreatedVO uploadResourceFile(CreateManualResourceReq request, MultipartFile file) {
        long userId = StpUtil.getLoginIdAsLong();
        Long learningSpaceId = request.getLearningSpaceId();

        // 1. 验证文件
        validateFile(file);

        // 验证分片策略是否有效
        if (!SliceStrategyEnum.isValid(request.getSliceStrategy())) {
            throw new ApplicationException(ResourceErrorCodeEnum.INVALID_SLICE_STRATEGY);
        }

        // 验证学习空间并创建新资源（其会设置ResourceStatusEnum.PENDING_PARSE.getCode()）
        ResourceEntity newResource = validateAndCreateResource(
                learningSpaceId,
                userId,
                request.getTitle(),
                request.getDescription(),
                ResourceSourceTypeEnum.UPLOAD.getCode(),
                null);

        // 上传文件
        InfraFileMetadata fileMetadata = filePort.upload(file, String.valueOf(userId), AccessLevel.PRIVATE);
        if (fileMetadata == null || fileMetadata.getFmStoragePath() == null || fileMetadata.getFmStoragePath().isBlank()) {
            throw new ApplicationException(ResourceErrorCodeEnum.FILE_UPLOAD_FAILED);
        }

        // 更新资源的源URI
        newResource.setRsSourceUri(fileMetadata.getFmStoragePath());

        // 保存资源
        resourceRepository.save(newResource);

        // 提交异步任务进行文件解析和分片
        Long taskId = resourceTaskPort.submitParseTask(newResource, SliceStrategyEnum.fromCode(request.getSliceStrategy()), String.valueOf(userId));

        // 返回资源ID
        return new ResourceCreatedVO(newResource.getRsId(), taskId);
    }

    /**
     * 从链接创建资源
     *
     * @param request 创建资源请求
     * @return 新资源ID
     */
    @Override
    public ResourceCreatedVO createResourceFromLink(CreateLinkResourceReq request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long learningSpaceId = request.getLearningSpaceId();

        // 验证分片策略是否有效
        if (!SliceStrategyEnum.isValid(request.getSliceStrategy())) {
            throw new ApplicationException(ResourceErrorCodeEnum.INVALID_SLICE_STRATEGY);
        }

        // 验证学习空间并创建新资源（其会设置ResourceStatusEnum.PENDING_PARSE.getCode()）
        ResourceEntity newResource = validateAndCreateResource(
                learningSpaceId,
                userId,
                request.getTitle(),
                request.getDescription(),
                ResourceSourceTypeEnum.LINK.getCode(),
                request.getUrl());

        // 保存资源
        resourceRepository.save(newResource);

        // 4. 异步提交链接抓取和解析任务
        Long taskId = resourceTaskPort.submitParseTask(newResource, SliceStrategyEnum.fromCode(request.getSliceStrategy()), String.valueOf(userId));

        // 5. 返回资源ID
        return new ResourceCreatedVO(newResource.getRsId(), taskId);
    }

    /**
     * 使用AI生成资源
     *
     * @param request 创建资源请求
     * @return 新资源ID
     */
    @Override
    public ResourceCreatedVO createAiSearchResource(CreateAiSearchResourceReq request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long learningSpaceId = request.getLearningSpaceId();

        // 1. 验证学习空间并创建新资源（其会设置ResourceStatusEnum.PENDING_PARSE.getCode()）
        ResourceEntity newResource = validateAndCreateResource(
                learningSpaceId,
                userId,
                request.getTitle(),
                request.getDescription(),
                ResourceSourceTypeEnum.AI_GENERATED.getCode(),
                null);

        // 2. 设置AI生成要求
        newResource.setRsPrompt(request.getRequirementPrompt());

        // 3. 保存资源
        resourceRepository.save(newResource);

        // 4. 异步提交内容解析和分片任务
        Long taskId = resourceTaskPort.submitAiGenerateTask(newResource, String.valueOf(userId));

        // 5. 返回资源ID
        return new ResourceCreatedVO(newResource.getRsId(), taskId);
    }

    @Override
    public void updateResource(Long resourceId, UpdateResourceReq request) {
        // 1. 验证资源是否存在，以及用户是否有权限操作
        long userId = StpUtil.getLoginIdAsLong();
        ResourceEntity resourceEntity = resourceRepository.findById(resourceId, List.of(ResourceEntity::getRsCreatedByUserId))
                .orElseThrow(() -> new ApplicationException(ResourceErrorCodeEnum.RESOURCE_NOT_FOUND));
        if (!resourceEntity.getRsCreatedByUserId().equals(userId)) {
            throw new ApplicationException(ResourceErrorCodeEnum.NO_PERMISSION_TO_OPERATE);
        }

        // 2. 更新资源信息
        resourceRepository.updateById(resourceId,
                updater -> {
                    updater.setRsTitle(request.getTitle());
                    updater.setRsDescription(request.getDescription());
                });
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId) {
        // 1. 验证资源是否存在，以及用户是否有权限操作
        long userId = StpUtil.getLoginIdAsLong();
        ResourceEntity resourceEntity = resourceRepository.findById(resourceId, List.of(ResourceEntity::getRsCreatedByUserId))
                .orElseThrow(() -> new ApplicationException(ResourceErrorCodeEnum.RESOURCE_NOT_FOUND));
        if (!resourceEntity.getRsCreatedByUserId().equals(userId)) {
            throw new ApplicationException(ResourceErrorCodeEnum.NO_PERMISSION_TO_OPERATE);
        }

        // 2. 查找该资源的所有分片
        List<ResourceChunkEntity> chunks = resourceChunkRepository.findByResourceId(resourceId);

        // 3. 删除已向量化的分片的向量数据
        List<ResourceChunkEntity> vectorizedChunks = chunks.stream()
                .filter(c -> Boolean.TRUE.equals(c.getRcIsVectorized()) && c.getRcVectorId() != null)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(vectorizedChunks)) {
            vectorizedChunks.stream()
                    .collect(Collectors.groupingBy(ResourceChunkEntity::getRcVectorDimension))
                    .forEach((dimension, chunkList) -> {
                        List<String> vectorIds = chunkList.stream().map(ResourceChunkEntity::getRcVectorId).collect(Collectors.toList());
                        vectorPort.batchDelete(vectorIds, dimension);
                    });
        }

        // 4. 删除所有分片
        resourceChunkRepository.deleteByResourceId(resourceId);

        // 5. 删除资源
        resourceRepository.deleteById(resourceId);
    }

    @Override
    public Long createResourceChunk(Long resourceId, CreateChunkReq request) {
        // 1. 验证资源是否存在，以及用户是否有权限操作
        long userId = StpUtil.getLoginIdAsLong();
        ResourceEntity resourceEntity = resourceRepository.findById(resourceId, List.of(ResourceEntity::getRsCreatedByUserId,ResourceEntity::getRsLearningSpaceId))
                .orElseThrow(() -> new ApplicationException(ResourceErrorCodeEnum.RESOURCE_NOT_FOUND));
        if (!resourceEntity.getRsCreatedByUserId().equals(userId)) {
            throw new ApplicationException(ResourceErrorCodeEnum.NO_PERMISSION_TO_OPERATE);
        }

        // 2. 创建并保存分片实体
        ResourceChunkEntity newChunk = new ResourceChunkEntity();
        newChunk.setRcResourceId(resourceId);
        newChunk.setRcLearningSpaceId(resourceEntity.getRsLearningSpaceId());
        newChunk.setRcContent(request.getContent());
        newChunk.setRcPageIndex(request.getPageIndex());
        newChunk.setRcChunkIndex(request.getChunkIndex());
        resourceChunkRepository.save(newChunk);

        // 3. 返回新分片ID
        return newChunk.getRcId();
    }

    @Override
    public void updateResourceChunk(Long chunkId, UpdateChunkReq request) {
        // 1. 验证分片是否存在，以及用户是否有权限操作
        ResourceChunkEntity chunk = findChunkAndCheckPermission(chunkId);

        // 2. 如果已向量化，则作废向量
        if (Boolean.TRUE.equals(chunk.getRcIsVectorized()) && chunk.getRcVectorId() != null) {
            vectorPort.delete(chunk.getRcVectorId(), chunk.getRcVectorDimension());
        }

        // 3. 更新分片内容
        resourceChunkRepository.updateById(chunkId,
                updater -> {
                    updater.setRcContent(request.getContent());
                    updater.setRcIsVectorized(false);
                });
    }

    @Override
    public Long triggerChunkVectorization(Long chunkId) {
        // 1. 验证分片是否存在
        ResourceChunkEntity chunk = findChunkAndCheckPermission(chunkId);
        if (Boolean.TRUE.equals(chunk.getRcIsVectorized())) {
            throw new ApplicationException(ResourceErrorCodeEnum.CHUNK_ALREADY_VECTORIZED);
        }

        // 2. 提交异步任务进行向量化处理
        return resourceTaskPort.submitVectorizeTask(chunk, String.valueOf(StpUtil.getLoginIdAsLong()));
    }

    @Override
    public void deleteResourceChunk(Long chunkId) {
        // 1. 验证分片是否存在，以及用户是否有权限操作
        ResourceChunkEntity chunk = findChunkAndCheckPermission(chunkId);

        // 2. 如果已向量化，先删除向量
        if (Boolean.TRUE.equals(chunk.getRcIsVectorized()) && chunk.getRcVectorId() != null) {
            vectorPort.delete(chunk.getRcVectorId(), chunk.getRcVectorDimension());
        }

        // 3. 删除分片记录
        resourceChunkRepository.deleteById(chunkId);
    }

    @Override
    public void cancelChunkVectorization(Long chunkId) {
        // 1. 验证分片是否存在，以及用户是否有权限操作
        ResourceChunkEntity chunk = findChunkAndCheckPermission(chunkId);

        // 2. 如果已向量化，则作废向量
        if (Boolean.TRUE.equals(chunk.getRcIsVectorized()) && chunk.getRcVectorId() != null) {
            vectorPort.delete(chunk.getRcVectorId(), chunk.getRcVectorDimension());
        }

        // 3. 更新分片内容
        resourceChunkRepository.updateById(chunkId,
                updater -> updater.setRcIsVectorized(false));
    }

    @Override
    public Long triggerBatchChunkVectorization(BatchVectorizeReq request) {
        List<Long> chunkIds = request.getChunkIds();
        if (CollectionUtils.isEmpty(chunkIds)) {
            return null;
        }
        // 权限检查
        List<ResourceChunkEntity> chunks = resourceChunkRepository.findByIds(chunkIds);
        long userId = StpUtil.getLoginIdAsLong();
        chunks.forEach(chunk -> {
            if (!chunk.getRcCreatedByUserId().equals(userId)) {
                throw new ApplicationException(ResourceErrorCodeEnum.NO_PERMISSION_TO_OPERATE);
            }
        });

        return resourceTaskPort.submitBatchVectorizeTask(chunkIds, String.valueOf(userId));
    }

    @Override
    @Transactional
    public void cancelBatchChunkVectorization(BatchCancelVectorizationReq request) {
        List<Long> chunkIds = request.getChunkIds();
        if (CollectionUtils.isEmpty(chunkIds)) {
            return;
        }

        // 批量权限检查
        List<ResourceChunkEntity> chunks = resourceChunkRepository.findByIds(chunkIds);
        long userId = StpUtil.getLoginIdAsLong();
        chunks.forEach(chunk -> {
            if (!chunk.getRcCreatedByUserId().equals(userId)) {
                throw new ApplicationException(ResourceErrorCodeEnum.NO_PERMISSION_TO_OPERATE);
            }
        });

        List<ResourceChunkEntity> chunksToCancel = chunks.stream()
                .filter(c -> Boolean.TRUE.equals(c.getRcIsVectorized()) && c.getRcVectorId() != null)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(chunksToCancel)) {
            return;
        }

        // 按维度分组并批量删除
        chunksToCancel.stream()
                .collect(Collectors.groupingBy(ResourceChunkEntity::getRcVectorDimension))
                .forEach((dimension, chunkList) -> {
                    List<String> vectorIds = chunkList.stream().map(ResourceChunkEntity::getRcVectorId).collect(Collectors.toList());
                    vectorPort.batchDelete(vectorIds, dimension);
                });

        // 批量更新数据库
        chunksToCancel.forEach(chunk -> chunk.setRcIsVectorized(false));
        resourceChunkRepository.saveBatch(chunksToCancel);
    }

    /**
     * 验证用户对指定分片的权限
     *
     * @param chunkId 分片的ID
     * @return 分片实体
     * @throws ApplicationException 如果用户没有权限访问该分片，则抛出异常
     */
    private ResourceChunkEntity findChunkAndCheckPermission(Long chunkId) {
        long userId = StpUtil.getLoginIdAsLong();
        ResourceChunkEntity chunk = resourceChunkRepository.findById(chunkId, List.of(
                        ResourceChunkEntity::getRcCreatedByUserId,
                        ResourceChunkEntity::getRcIsVectorized,
                        ResourceChunkEntity::getRcVectorId
                ))
                .orElseThrow(() -> new ApplicationException(ResourceErrorCodeEnum.CHUNK_NOT_FOUND));

        if (!chunk.getRcCreatedByUserId().equals(userId)) {
            throw new ApplicationException(ResourceErrorCodeEnum.CHUNK_NOT_FOUND);
        }
        return chunk;
    }

    /**
     * 验证上传的文件是否符合要求
     *
     * @param file 用户上传的文件，不能为空，且文件类型必须在允许的范围内
     * @throws ApplicationException 如果文件为空或文件类型不支持，则抛出异常
     */
    private void validateFile(MultipartFile file) {
        // 检查文件是否为空，如果为空，则抛出异常
        if (file.isEmpty()) {
            throw new ApplicationException(ResourceErrorCodeEnum.FILE_EMPTY);
        }

        // 获取文件扩展名，并检查是否在允许的文件类型列表中
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        // 如果扩展名为空或不在允许的列表中，则抛出异常
        if (extension == null || !ALLOWED_FILE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ApplicationException(ResourceErrorCodeEnum.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    /**
     * 验证学习空间并创建新资源对象
     *
     * @param learningSpaceId 学习空间ID
     * @param userId          用户ID
     * @param title           资源标题
     * @param description     资源描述
     * @param sourceType      源类型
     * @param sourceUri       源URI
     * @return 新建的资源实体
     */
    private ResourceEntity validateAndCreateResource(
            Long learningSpaceId,
            Long userId,
            String title,
            String description,
            Integer sourceType,
            String sourceUri) {
        // 1. 验证学习空间是否存在且属于当前用户
        if (!learningSpaceRepository.existsByIdAndUserId(learningSpaceId, userId)) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_ERROR, "学习空间不存在或无权访问");
        }

        // 2. 创建新资源
        ResourceEntity newResource = new ResourceEntity();
        newResource.setRsLearningSpaceId(learningSpaceId);
        newResource.setRsTitle(title);
        newResource.setRsDescription(description);
        newResource.setRsSourceType(sourceType);
        newResource.setRsSourceUri(sourceUri);
        newResource.setRsCreatedByUserId(userId);
        newResource.setRsStatus(ResourceStatusEnum.PENDING_PARSE.getCode());

        return newResource;
    }
}

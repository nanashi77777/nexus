package cn.lin037.nexus.application.knowledge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.knowledge.enums.KnowledgeErrorCodeEnum;
import cn.lin037.nexus.application.knowledge.port.KnowledgeFolderRepository;
import cn.lin037.nexus.application.knowledge.service.KnowledgeFolderAppService;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgeFolderEntity;
import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateFolderReq;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgeFolderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class KnowledgeFolderAppServiceImpl implements KnowledgeFolderAppService {

    private final KnowledgeFolderRepository folderRepository;

    public KnowledgeFolderAppServiceImpl(KnowledgeFolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    /**
     * 创建文件夹
     *
     * @param req 创建文件夹请求参数
     * @return 文件夹信息
     */
    @Override
    public KnowledgeFolderVO createFolder(CreateFolderReq req) {
        long userId = StpUtil.getLoginIdAsLong();

        KnowledgeFolderEntity entity = new KnowledgeFolderEntity();
        entity.setKfId(HutoolSnowflakeIdGenerator.generateLongId());
        entity.setKfParentId(req.getParentId());
        entity.setKfName(req.getName());
        entity.setKfCreatedByUserId(userId);
        entity.setKfLearningSpaceId(req.getLearningSpaceId());
        entity.setKfCreatedAt(LocalDateTime.now());
        entity.setKfUpdatedAt(LocalDateTime.now());

        folderRepository.save(entity);

        return KnowledgeFolderVO.fromEntity(entity);
    }

    /**
     * 重命名文件夹
     *
     * @param folderId 文件夹ID
     * @param newName  新的文件夹名称
     */
    @Override
    public void renameFolder(Long folderId, String newName) {
        long userId = StpUtil.getLoginIdAsLong();

        folderRepository.rename(folderId, userId, newName);
    }

    /**
     * 移动文件夹
     *
     * @param folderId          文件夹ID
     * @param newParentFolderId 新的父文件夹ID
     */
    @Override
    public void moveFolder(Long folderId, Long newParentFolderId) {
        long userId = StpUtil.getLoginIdAsLong();
        folderRepository.moveFolderOptimized(folderId, newParentFolderId, userId);
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId) {
        long userId = StpUtil.getLoginIdAsLong();
        KnowledgeFolderEntity folder = folderRepository.findById(folderId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_NOT_FOUND));
        if (!folder.getKfCreatedByUserId().equals(userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FORBIDDEN);
        }

        // TODO: 检查文件夹下是否有子文件夹或文件，如果有则不允许删除

        folderRepository.deleteById(folderId);
    }

    @Override
    public java.util.List<KnowledgeFolderVO> getFolders(Long learningSpaceId, Long parentId) {
        long userId = StpUtil.getLoginIdAsLong();
        return folderRepository.findByLearningSpaceIdAndParentId(learningSpaceId, parentId, userId, Collections.emptyList())
                .stream()
                .map(KnowledgeFolderVO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

}

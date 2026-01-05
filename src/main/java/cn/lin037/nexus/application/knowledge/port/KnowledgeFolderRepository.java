package cn.lin037.nexus.application.knowledge.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgeFolderEntity;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;

/**
 * 知识点文件夹仓储接口
 *
 * @author LinSanQi
 */
public interface KnowledgeFolderRepository {

    /**
     * 根据ID和用户ID判断文件夹是否存在
     *
     * @param id     文件夹ID
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * 根据父文件夹ID和名称判断是否存在
     *
     * @param parentId 父文件夹ID
     * @param name     文件夹名称
     * @param userId   用户ID
     * @return 是否存在
     */
    boolean existsByParentIdAndNameAndUserId(Long parentId, String name, Long userId);

    /**
     * 根据ID和用户ID查找文件夹
     *
     * @param id      文件夹ID
     * @param userId  用户ID
     * @param getters 查询字段
     * @return 文件夹实体
     */
    Optional<KnowledgeFolderEntity> findByIdAndUserId(Long id, Long userId, List<Getter<KnowledgeFolderEntity>> getters);

    /**
     * 根据ID查找文件夹
     *
     * @param id      文件夹ID
     * @param getters 查询字段
     * @return 文件夹实体
     */
    Optional<KnowledgeFolderEntity> findById(Long id, List<Getter<KnowledgeFolderEntity>> getters);

    /**
     * 根据父文件夹ID查找子文件夹列表
     *
     * @param parentId 父文件夹ID
     * @param userId   用户ID
     * @param getters  查询字段
     * @return 子文件夹列表
     */
    List<KnowledgeFolderEntity> findByParentId(Long parentId, Long userId, List<Getter<KnowledgeFolderEntity>> getters);

    /**
     * 根据学习空间ID和父文件夹ID查找文件夹列表
     *
     * @param learningSpaceId 学习空间ID
     * @param parentId        父文件夹ID
     * @param userId          用户ID
     * @param getters         查询字段
     * @return 文件夹列表
     */
    List<KnowledgeFolderEntity> findByLearningSpaceIdAndParentId(Long learningSpaceId, Long parentId, Long userId, List<Getter<KnowledgeFolderEntity>> getters);

    /**
     * 根据父文件夹ID与UserId统计子文件夹数量
     *
     * @param parentId 父文件夹ID
     * @param userId   用户ID
     * @return 子文件夹数量
     */
    int countByParentIdAndUserId(Long parentId, Long userId);

    /**
     * 保存文件夹
     *
     * @param entity 文件夹实体
     */
    void save(KnowledgeFolderEntity entity);

    /**
     * 移动文件夹
     *
     * @param folderId    要移动的文件夹ID
     * @param newParentId 新的父文件夹ID
     * @param userId      用户ID
     */
    void move(Long folderId, Long newParentId, Long userId);

    /**
     * 移动文件夹（优化版）
     *
     * @param folderId    要移动的文件夹ID
     * @param newParentId 新的父文件夹ID
     * @param userId      用户ID
     */
    void moveFolderOptimized(Long folderId, Long newParentId, Long userId);

    /**
     * 更新文件夹
     *
     * @param id      文件夹ID
     * @param userId  用户ID
     * @param newName 新的文件夹名称
     */
    void rename(Long id, Long userId, String newName);

    /**
     * 删除文件夹
     *
     * @param id 文件夹ID
     * @return 是否成功
     */
    boolean deleteById(Long id);

    /**
     * 根据ID和用户ID查找学习空间ID
     *
     * @param folderId 文件夹ID
     * @param userId   用户ID
     * @return 学习空间ID
     */
    Optional<Long> findLearningSpaceIdByIdAndUserId(Long folderId, Long userId);
}

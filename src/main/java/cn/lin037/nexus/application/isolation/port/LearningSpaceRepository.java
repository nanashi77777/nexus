package cn.lin037.nexus.application.isolation.port;

import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.common.persistent.entity.LearningSpaceEntity;
import cn.lin037.nexus.web.rest.v1.isolation.req.LearningSpacePageQuery;
import com.sun.istack.NotNull;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 学习空间仓储接口
 * 负责与学习空间相关的持久化操作
 *
 * @author GitHub Copilot
 */
public interface LearningSpaceRepository {

    // ====== 查询操作 ======

    /**
     * 根据ID和用户ID判断学习空间是否存在
     *
     * @param id     学习空间ID
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * 根据ID查找学习空间
     *
     * @param id      学习空间ID
     * @param getters 查询字段
     * @return 学习空间实体
     */
    Optional<LearningSpaceEntity> findById(Long id, List<Getter<LearningSpaceEntity>> getters);

    /**
     * 根据用户ID分页查询学习空间列表
     * (新增方法定义)
     *
     * @param query  分页及查询参数
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<LearningSpaceEntity> findPageByUserId(LearningSpacePageQuery query, Long userId);


    // ====== 写操作 ======

    /**
     * 保存新的学习空间
     *
     * @param learningSpaceEntity 学习空间实体
     */
    void save(LearningSpaceEntity learningSpaceEntity);

    /**
     * 更新学习空间信息
     *
     * @param learningSpaceId 学习空间ID
     * @param updater         更新函数
     */
    void updateById(@NotNull Long learningSpaceId, Consumer<LearningSpaceEntity> updater);


    // ====== 删除操作 ======

    /**
     * 根据ID删除学习空间
     *
     * @param learningSpaceId 学习空间ID
     * @return 是否删除成功
     */
    Boolean deleteById(Long learningSpaceId);
}


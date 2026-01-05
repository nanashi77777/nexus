package cn.lin037.nexus.application.agent.port;

import cn.lin037.nexus.web.rest.v1.agent.req.AgentSessionPageQuery;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatSessionStatusEnum;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;

/**
 * Agent聊天会话Repository接口
 * 提供Agent聊天会话的管理操作
 *
 * @author Lin037
 */
public interface AgentChatSessionRepository {

    /**
     * 保存聊天会话
     *
     * @param entity 聊天会话实体
     * @return 保存后的实体
     */
    AgentChatSessionEntity save(AgentChatSessionEntity entity);

    /**
     * 根据ID查询聊天会话
     *
     * @param id 会话ID
     * @param getters 需要查询的字段
     * @return 聊天会话实体
     */
    Optional<AgentChatSessionEntity> findById(Long id, List<Getter<AgentChatSessionEntity>> getters);

    /**
     * 根据ID查询聊天会话
     *
     * @param id 会话ID
     * @param getters 需要查询的字段
     * @return 聊天会话实体
     */
    Optional<AgentChatSessionEntity> findByIdAndUserId(Long id, Long userId, List<Getter<AgentChatSessionEntity>> getters);

    /**
     * 更新聊天会话
     *
     * @param entity 聊天会话实体
     * @return 更新后的实体
     */
    AgentChatSessionEntity updateById(AgentChatSessionEntity entity);

    /**
     * 更新会话状态
     *
     * @param id     会话ID
     * @param expectedStatus    旧状态
     * @param newStatus 新状态
     * @return 是否更新成功
     */
    boolean updateStatusAtomically(Long id, AgentChatSessionStatusEnum expectedStatus, AgentChatSessionStatusEnum newStatus);

    /**
     * 更新会话状态
     *
     * @param id     会话ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateStatus(Long id, AgentChatSessionStatusEnum status);

    /**
     * 删除聊天会话
     *
     * @param id 会话ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 分页查询聊天会话（使用AgentPageQuery）
     *
     * @param userId 用户ID
     * @param query 分页查询参数
     * @return 分页结果
     */
    PageResult<AgentChatSessionEntity> findPageByUserId(Long userId, AgentSessionPageQuery query);

}

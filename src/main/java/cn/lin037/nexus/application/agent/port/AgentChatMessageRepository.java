package cn.lin037.nexus.application.agent.port;

import cn.lin037.nexus.web.rest.v1.agent.req.AgentMessagePageQuery;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMessageRoleEnum;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;

/**
 * Agent聊天消息Repository接口
 * 提供Agent聊天消息的管理操作
 *
 * @author Lin037
 */
public interface AgentChatMessageRepository {

    /**
     * 保存聊天消息
     *
     * @param entity 聊天消息实体
     * @return 保存后的实体
     */
    AgentChatMessageEntity save(AgentChatMessageEntity entity);

    /**
     * 根据ID查询聊天消息
     *
     * @param id 聊天消息ID
     * @return 聊天消息实体
     */
    Optional<AgentChatMessageEntity> findById(Long id);

    /**
     * 根据ID查询聊天消息
     *
     * @param id 聊天消息ID
     * @param getters 需要查询的字段
     * @return 聊天消息实体
     */
    Optional<AgentChatMessageEntity> findById(Long id, List<Getter<AgentChatMessageEntity>> getters);

    /**
     * 根据会话ID查询聊天消息列表
     *
     * @param sessionId 会话ID
     * @return 聊天消息列表
     */
    List<AgentChatMessageEntity> findBySessionId(Long sessionId);

    /**
     * 根据会话ID查询聊天消息列表
     *
     * @param sessionId 会话ID
     * @param getters 需要查询的字段
     * @return 聊天消息列表
     */
    List<AgentChatMessageEntity> findBySessionId(Long sessionId, List<Getter<AgentChatMessageEntity>> getters);

    /**
     * 更新聊天消息
     *
     * @param entity 聊天消息实体
     * @return 更新后的实体
     */
    AgentChatMessageEntity updateById(AgentChatMessageEntity entity);

    /**
     * 删除聊天消息
     *
     * @param id 消息ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据会话ID删除聊天消息
     *
     * @param sessionId 会话ID
     */
    void deleteBySessionId(Long sessionId);

    /**
     * 检查聊天消息是否存在
     *
     * @param id 消息ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    Optional<AgentChatMessageEntity> findLatestMessageBySessionIdAndRole(Long sessionId, AgentChatMessageRoleEnum role);

    /**
     * 根据用户ID分页查询聊天消息
     *
     * @param query 分页查询参数
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<AgentChatMessageEntity> findPageByUserId(Long userId, AgentMessagePageQuery query);
}
package cn.lin037.nexus.application.agent.port;

import java.time.Duration;

/**
 * Agent缓存端口接口
 * 提供会话状态、取消标志等缓存操作
 *
 * @author Lin037
 */
public interface AgentCachePort {

    /**
     * 设置会话取消标志
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param learningSpaceId 学习空间ID
     * @param expiration 过期时间
     */
    void setSessionCancelled(Long sessionId, Long userId, Long learningSpaceId, Duration expiration);

    /**
     * 检查会话是否已被取消
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param learningSpaceId 学习空间ID
     * @return 是否已取消
     */
    boolean isSessionCancelled(Long sessionId, Long userId, Long learningSpaceId);

    /**
     * 移除会话取消标志
     *
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param learningSpaceId 学习空间ID
     */
    void removeSessionCancellation(Long sessionId, Long userId, Long learningSpaceId);

}

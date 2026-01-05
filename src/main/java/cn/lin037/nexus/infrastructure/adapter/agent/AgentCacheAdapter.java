package cn.lin037.nexus.infrastructure.adapter.agent;

import cn.lin037.nexus.application.agent.port.AgentCachePort;
import cn.lin037.nexus.infrastructure.common.cache.service.CacheService;
import cn.lin037.nexus.infrastructure.common.cache.util.CacheKeyBuilder;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Agent缓存适配器
 * 用于处理会话取消相关的缓存操作
 */
@Service
@RequiredArgsConstructor
public class AgentCacheAdapter implements AgentCachePort {

    /**
     * 缓存服务，使用Redisson实现
     */
    @Resource(name = "RedissonCacheService")
    private final CacheService cacheService;

    /**
     * 设置会话取消状态
     *
     * @param sessionId       会话ID
     * @param userId          用户ID
     * @param learningSpaceId 学习空间ID
     * @param expiration      过期时间
     */
    @Override
    public void setSessionCancelled(Long sessionId, Long userId, Long learningSpaceId, Duration expiration) {
        String sessionCancellationKey = getSessionCancellationKey(sessionId, userId, learningSpaceId);
        cacheService.set(sessionCancellationKey, Boolean.TRUE, expiration.toMinutes(), TimeUnit.MINUTES);
    }

    /**
     * 检查会话是否已取消
     *
     * @param sessionId       会话ID
     * @param userId          用户ID
     * @param learningSpaceId 学习空间ID
     * @return 如果会话已取消返回true，否则返回false
     */
    @Override
    public boolean isSessionCancelled(Long sessionId, Long userId, Long learningSpaceId) {
        String sessionCancellationKey = getSessionCancellationKey(sessionId, userId, learningSpaceId);
        Boolean cancelled = cacheService.get(sessionCancellationKey);
        return Boolean.TRUE.equals(cancelled);
    }

    /**
     * 移除会话取消状态
     *
     * @param sessionId       会话ID
     * @param userId          用户ID
     * @param learningSpaceId 学习空间ID
     */
    @Override
    public void removeSessionCancellation(Long sessionId, Long userId, Long learningSpaceId) {
        String sessionCancellationKey = getSessionCancellationKey(sessionId, userId, learningSpaceId);
        cacheService.delete(sessionCancellationKey);
    }

    /**
     * 生成会话取消状态的缓存键
     *
     * @param sessionId       会话ID
     * @param userId          用户ID
     * @param learningSpaceId 学习空间ID
     * @return 缓存键
     */
    private String getSessionCancellationKey(Long sessionId, Long userId, Long learningSpaceId) {
        return CacheKeyBuilder.buildKey("session_cancellation", sessionId.toString(), userId.toString(), learningSpaceId.toString());
    }
}

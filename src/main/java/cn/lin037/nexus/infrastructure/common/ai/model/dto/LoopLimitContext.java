package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 循环限制上下文接口
 * 用于在流式聊天工具服务中限制循环调用次数，防止无限循环的发生。
 * 实现此接口的上下文对象将具备循环次数统计和限制的能力。
 *
 * @author Lin037
 */
public interface LoopLimitContext {

    /**
     * 默认最大循环次数限制
     */
    int DEFAULT_MAX_LOOP_COUNT = 10;

    /**
     * 获取循环计数器
     * 返回一个线程安全的原子整数计数器，用于统计当前的循环次数。
     *
     * @return 循环计数器
     */
    AtomicInteger getLoopCounter();

    /**
     * 获取最大循环次数限制
     * 默认实现返回10次，子类可以重写此方法来自定义限制。
     *
     * @return 最大循环次数
     */
    default int getMaxLoopCount() {
        return DEFAULT_MAX_LOOP_COUNT;
    }

    /**
     * 检查是否已达到循环限制
     * 比较当前循环次数与最大限制，判断是否应该停止循环。
     *
     * @return 如果已达到限制返回true，否则返回false
     */
    default boolean isLoopLimitReached() {
        return getLoopCounter().get() >= getMaxLoopCount();
    }

    /**
     * 递增循环计数器
     * 原子性地将循环计数器加1，并返回递增后的值。
     */
    default void incrementLoopCount() {
        getLoopCounter().incrementAndGet();
    }

    /**
     * 重置循环计数器
     * 将循环计数器重置为0，通常在新的对话会话开始时调用。
     */
    default void resetLoopCount() {
        getLoopCounter().set(0);
    }

    /**
     * 获取当前循环次数
     * 返回当前的循环计数值。
     *
     * @return 当前循环次数
     */
    default int getCurrentLoopCount() {
        return getLoopCounter().get();
    }
}
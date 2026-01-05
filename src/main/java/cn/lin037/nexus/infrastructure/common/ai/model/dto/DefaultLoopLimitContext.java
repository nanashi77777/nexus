package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认循环限制上下文实现
 * 提供基本的循环次数限制功能
 *
 * @author Lin037
 */
public class DefaultLoopLimitContext implements LoopLimitContext {

    /**
     * 循环计数器
     */
    private final AtomicInteger loopCounter = new AtomicInteger(0);

    /**
     * 最大循环次数
     */
    private final int maxLoopCount;

    /**
     * 默认构造函数，使用默认的最大循环次数
     */
    public DefaultLoopLimitContext() {
        this.maxLoopCount = DEFAULT_MAX_LOOP_COUNT;
    }

    /**
     * 构造函数，指定最大循环次数
     *
     * @param maxLoopCount 最大循环次数
     */
    public DefaultLoopLimitContext(int maxLoopCount) {
        this.maxLoopCount = maxLoopCount;
    }

    @Override
    public AtomicInteger getLoopCounter() {
        return loopCounter;
    }

    @Override
    public int getMaxLoopCount() {
        return maxLoopCount;
    }
}
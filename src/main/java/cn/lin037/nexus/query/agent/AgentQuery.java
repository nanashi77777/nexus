package cn.lin037.nexus.query.agent;

import cn.lin037.nexus.query.agent.vo.LearningTaskPageVO;
import cn.lin037.nexus.query.agent.vo.MemoryPageVO;
import cn.lin037.nexus.query.agent.vo.MessagePageVO;
import cn.lin037.nexus.query.agent.vo.SessionPageVO;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentLearningTaskPageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentMemoryPageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentMessagePageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentSessionPageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;

/**
 * Agent查询接口
 * 提供Agent相关数据的查询功能
 *
 * @author Lin037
 */
public interface AgentQuery {

    /**
     * 分页查询会话
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    Pager<SessionPageVO> findUserSessionsPage(AgentSessionPageQuery query);

    /**
     * 分页查询记忆
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    Pager<MemoryPageVO> findUserMemoriesPage(AgentMemoryPageQuery query);

    /**
     * 分页查询学习任务
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    Pager<LearningTaskPageVO> findUserLearningTasksPage(AgentLearningTaskPageQuery query);

    /**
     * 分页查询消息
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    Pager<MessagePageVO> findUserMessagesPage(AgentMessagePageQuery query);
}

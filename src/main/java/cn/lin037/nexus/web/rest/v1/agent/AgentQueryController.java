package cn.lin037.nexus.web.rest.v1.agent;

import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.query.agent.AgentQuery;
import cn.lin037.nexus.query.agent.vo.LearningTaskPageVO;
import cn.lin037.nexus.query.agent.vo.MemoryPageVO;
import cn.lin037.nexus.query.agent.vo.MessagePageVO;
import cn.lin037.nexus.query.agent.vo.SessionPageVO;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentLearningTaskPageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentMemoryPageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentMessagePageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentSessionPageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent查询控制器
 * 提供Agent相关数据的查询API接口
 *
 * @author Lin037
 */
@RestController
@RequestMapping("/api/v1/agent/query")
@RequiredArgsConstructor
public class AgentQueryController {

    private final AgentQuery agentQuery;

    // ==================== Session查询接口 ====================

    /**
     * 分页查询会话
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/sessions")
    public ResultVO<Pager<SessionPageVO>> findSessionPage(
            @ModelAttribute @Valid AgentSessionPageQuery query) {
        query.valid();
        Pager<SessionPageVO> result = agentQuery.findUserSessionsPage(query);
        return ResultVO.success(result);
    }

    // ==================== Memory查询接口 ====================

    /**
     * 分页查询记忆
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/memories")
    public ResultVO<Pager<MemoryPageVO>> findMemoryPage(
            @ModelAttribute @Valid AgentMemoryPageQuery query) {
        query.valid();
        Pager<MemoryPageVO> result = agentQuery.findUserMemoriesPage(query);
        return ResultVO.success(result);
    }

    // ==================== LearningTask查询接口 ====================

    /**
     * 分页查询学习任务
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/learning-tasks")
    public ResultVO<Pager<LearningTaskPageVO>> findLearningTaskPage(
            @ModelAttribute @Valid AgentLearningTaskPageQuery query) {
        query.valid();
        Pager<LearningTaskPageVO> result = agentQuery.findUserLearningTasksPage(query);
        return ResultVO.success(result);
    }

    // ==================== Message查询接口 ====================

    /**
     * 分页查询消息
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/messages")
    public ResultVO<Pager<MessagePageVO>> findMessagePage(
            @ModelAttribute @Valid AgentMessagePageQuery query) {
        query.valid();
        Pager<MessagePageVO> result = agentQuery.findUserMessagesPage(query);
        return ResultVO.success(result);
    }
}

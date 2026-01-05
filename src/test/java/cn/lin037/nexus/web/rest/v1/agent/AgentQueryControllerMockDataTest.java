package cn.lin037.nexus.web.rest.v1.agent;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import cn.lin037.nexus.web.rest.v1.agent.vo.LearningTaskVO;
import cn.lin037.nexus.web.rest.v1.agent.vo.MemoryVO;
import cn.lin037.nexus.web.rest.v1.agent.vo.MessageVO;
import cn.lin037.nexus.web.rest.v1.agent.vo.SessionVO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * AgentQueryController模拟数据生成测试
 * 用于生成前端开发所需的示例响应数据
 *
 * @author Lin037
 */
public class AgentQueryControllerMockDataTest {

    private static final String OUTPUT_DIR = "mock-data";
    private static final LocalDateTime NOW = LocalDateTime.now();

    /**
     * 生成会话分页查询的模拟数据
     */
    @Test
    public void generateSessionPageMockData() throws IOException {
        // 创建模拟会话数据
        List<SessionVO> sessions = Arrays.asList(
                createSessionVO(1001L, "Java学习讨论", 2, 1, 4, 3),
                createSessionVO(1002L, "Spring Boot项目实战", 2, 1, 4, 1),
                createSessionVO(1003L, "数据库设计咨询", 1, 1, 1, 2),
                createSessionVO(1004L, "算法问题求解", 1, 1, 2, 4),
                createSessionVO(1005L, "前端技术交流", 1, 2, 3, 1)
        );

        // 创建分页结果
        PageResult<SessionVO> pageResult = PageResult.of(sessions, 25L, 1, 10);

        // 创建API响应结果
        ResultVO<PageResult<SessionVO>> result = ResultVO.success(pageResult);

        // 转换为JSON并保存
        String json = JSONUtil.toJsonPrettyStr(result);
        saveToFile("session-page-response.json", json);

        System.out.println("会话分页查询模拟数据已生成: " + OUTPUT_DIR + "/session-page-response.json");
    }

    /**
     * 生成记忆分页查询的模拟数据
     */
    @Test
    public void generateMemoryPageMockData() throws IOException {
        // 创建模拟记忆数据
        List<MemoryVO> memories = Arrays.asList(
                createMemoryVO(2001L, 1001L, 2, "Java基础知识点", "Java是一种面向对象的编程语言，具有跨平台特性。", 8, Arrays.asList("Java", "基础"), "learning", 5),
                createMemoryVO(2002L, 1002L, 1, "Spring Boot配置", "Spring Boot提供了自动配置功能，简化了项目搭建。", 9, Arrays.asList("Spring Boot", "配置"), "chat", 3),
                createMemoryVO(2003L, null, 2, "数据库设计原则", "数据库设计应遵循范式理论，避免数据冗余。", 7, Arrays.asList("数据库", "设计"), "manual", 8),
                createMemoryVO(2004L, 1004L, 1, "算法复杂度", "时间复杂度和空间复杂度是衡量算法效率的重要指标。", 6, Arrays.asList("算法", "复杂度"), "learning", 2),
                createMemoryVO(2005L, 1005L, 2, "前端框架对比", "React、Vue、Angular各有优势，选择需根据项目需求。", 5, Arrays.asList("前端", "框架"), "chat", 1)
        );

        // 创建分页结果
        PageResult<MemoryVO> pageResult = PageResult.of(memories, 18L, 1, 10);

        // 创建API响应结果
        ResultVO<PageResult<MemoryVO>> result = ResultVO.success(pageResult);

        // 转换为JSON并保存
        String json = JSONUtil.toJsonPrettyStr(result);
        saveToFile("memory-page-response.json", json);

        System.out.println("记忆分页查询模拟数据已生成: " + OUTPUT_DIR + "/memory-page-response.json");
    }

    /**
     * 生成学习任务分页查询的模拟数据
     */
    @Test
    public void generateLearningTaskPageMockData() throws IOException {
        // 创建模拟学习任务数据
        List<LearningTaskVO> tasks = Arrays.asList(
                createLearningTaskVO(3001L, 1001L, "掌握Java集合框架", "深入理解ArrayList、HashMap等常用集合的实现原理和使用场景", AgentLearningDifficultyEnum.INTERMEDIATE, false),
                createLearningTaskVO(3002L, 1002L, "Spring Boot微服务实战", "学习使用Spring Boot构建微服务架构，包括服务注册、发现和配置管理", AgentLearningDifficultyEnum.ADVANCED, true),
                createLearningTaskVO(3003L, 1003L, "MySQL性能优化", "学习数据库索引优化、查询优化和分库分表等性能提升技术", AgentLearningDifficultyEnum.EXPERT, false),
                createLearningTaskVO(3004L, 1004L, "算法与数据结构基础", "掌握常见的排序算法、搜索算法和基本数据结构", AgentLearningDifficultyEnum.BEGINNER, true),
                createLearningTaskVO(3005L, 1005L, "React Hooks深入学习", "理解React Hooks的原理和最佳实践，提升前端开发能力", AgentLearningDifficultyEnum.INTERMEDIATE, false)
        );

        // 创建分页结果
        PageResult<LearningTaskVO> pageResult = PageResult.of(tasks, 32L, 1, 10);

        // 创建API响应结果
        ResultVO<PageResult<LearningTaskVO>> result = ResultVO.success(pageResult);

        // 转换为JSON并保存
        String json = JSONUtil.toJsonPrettyStr(result);
        saveToFile("learning-task-page-response.json", json);

        System.out.println("学习任务分页查询模拟数据已生成: " + OUTPUT_DIR + "/learning-task-page-response.json");
    }

    /**
     * 生成消息分页查询的模拟数据
     */
    @Test
    public void generateMessagePageMockData() throws IOException {
        // 创建模拟消息数据
        List<MessageVO> messages = Arrays.asList(
                createMessageVO(4001L, 1001L, 1, "你好，我想学习Java集合框架，应该从哪里开始？", 1, 1, null, null, null),
                createMessageVO(4002L, 1001L, 2, "你好！学习Java集合框架是个很好的选择。我建议你从以下几个方面开始：\n\n1. **List接口**：先学习ArrayList和LinkedList\n2. **Set接口**：了解HashSet和TreeSet的区别\n3. **Map接口**：重点掌握HashMap的实现原理\n\n你想从哪个部分开始呢？", 1, 1, 4001L, null, null),
                createMessageVO(4003L, 1001L, 1, "我想先从ArrayList开始，能详细介绍一下它的特点吗？", 1, 1, 4002L, null, null),
                createMessageVO(4004L, 1001L, 2, "ArrayList是基于动态数组实现的List，具有以下特点：\n\n**优点：**\n- 随机访问效率高，时间复杂度O(1)\n- 内存占用相对较小\n- 支持快速遍历\n\n**缺点：**\n- 插入和删除操作效率较低，时间复杂度O(n)\n- 初始容量不足时需要扩容，可能影响性能\n\n需要我为你创建一个学习任务来系统学习ArrayList吗？", 1, 1, 4003L, null, null),
                createMessageVO(4005L, 1001L, 1, "好的，请帮我创建一个ArrayList的学习任务。", 1, 1, 4004L, null, null)
        );

        // 创建分页结果
        PageResult<MessageVO> pageResult = PageResult.of(messages, 156L, 1, 20);

        // 创建API响应结果
        ResultVO<PageResult<MessageVO>> result = ResultVO.success(pageResult);

        // 转换为JSON并保存
        String json = JSONUtil.toJsonPrettyStr(result);
        saveToFile("message-page-response.json", json);

        System.out.println("消息分页查询模拟数据已生成: " + OUTPUT_DIR + "/message-page-response.json");
    }

    /**
     * 生成所有模拟数据
     */
    @Test
    public void generateAllMockData() throws IOException {
        generateSessionPageMockData();
        generateMemoryPageMockData();
        generateLearningTaskPageMockData();
        generateMessagePageMockData();

        System.out.println("\n所有模拟数据生成完成！文件保存在: " + OUTPUT_DIR + " 目录下");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建会话VO对象
     *
     * @param id           会话ID
     * @param title        会话标题
     * @param type         会话类型 (1:普通聊天, 2:学习会话)
     * @param status       会话状态 (1:正常, 2:响应中, 3:暂停, 4:工具调用中, 5:等待工具授权, 6:错误)
     * @param belongsTo    会话所属 (1:图谱, 2:讲解, 3:笔记, 4:学习)
     * @param autoCallTool 自动调用工具权限 (1:只读, 2:只写, 3:可读可写, 4:关闭)
     * @return SessionVO对象
     */
    private SessionVO createSessionVO(Long id, String title, Integer type, Integer status, Integer belongsTo, Integer autoCallTool) {
        SessionVO session = new SessionVO();
        session.setId(id);
        session.setUserId(10001L);
        session.setLearningSpaceId(20001L);
        session.setTitle(title);
        session.setType(type);
        session.setStatus(status);
        session.setBelongsTo(belongsTo);
        session.setIsAutoCallTool(autoCallTool);
        session.setCreatedAt(NOW.minusHours(id - 1000));
        session.setUpdatedAt(NOW.minusMinutes((id - 1000) * 30));
        return session;
    }

    /**
     * 创建记忆VO对象
     *
     * @param id              记忆ID
     * @param sessionId       会话ID (可为null表示全局记忆)
     * @param level           记忆等级 (0:不启用, 1:会话级, 2:全局)
     * @param title           记忆标题
     * @param content         记忆内容
     * @param importanceScore 重要性评分 (1-10)
     * @param tags            标签列表
     * @param source          记忆来源 (chat:聊天, learning:学习, manual:手动添加)
     * @param accessCount     访问次数
     * @return MemoryVO对象
     */
    private MemoryVO createMemoryVO(Long id, Long sessionId, Integer level, String title, String content,
                                    Integer importanceScore, List<String> tags, String source, Integer accessCount) {
        MemoryVO memory = new MemoryVO();
        memory.setId(id);
        memory.setUserId(10001L);
        memory.setLearningSpaceId(20001L);
        memory.setSessionId(sessionId);
        memory.setLevel(level);
        memory.setTitle(title);
        memory.setContent(content);
        memory.setImportanceScore(importanceScore);
        memory.setTags(tags);
        memory.setSource(source);
        memory.setAccessCount(accessCount);
        memory.setLastAccessedAt(NOW.minusHours(accessCount));
        memory.setCreatedAt(NOW.minusDays(id - 2000));
        memory.setUpdatedAt(NOW.minusHours((id - 2000) * 2));
        return memory;
    }

    /**
     * 创建学习任务VO对象
     *
     * @param id              任务ID
     * @param sessionId       会话ID
     * @param title           任务标题
     * @param objective       学习目标
     * @param difficultyLevel 难度等级
     * @param isCompleted     是否完成
     * @return LearningTaskVO对象
     */
    private LearningTaskVO createLearningTaskVO(Long id, Long sessionId, String title, String objective,
                                                AgentLearningDifficultyEnum difficultyLevel, Boolean isCompleted) {
        LearningTaskVO task = new LearningTaskVO();
        task.setId(id);
        task.setUserId(10001L);
        task.setLearningSpaceId(20001L);
        task.setSessionId(sessionId);
        task.setTitle(title);
        task.setObjective(objective);
        task.setDifficultyLevel(difficultyLevel);
        task.setIsCompleted(isCompleted);
        task.setCreatedAt(NOW.minusDays(id - 3000));
        task.setUpdatedAt(NOW.minusHours((id - 3000) * 3));
        return task;
    }

    /**
     * 创建消息VO对象
     *
     * @param id              消息ID
     * @param sessionId       会话ID
     * @param role            消息角色 (1:用户, 2:助手, 3:系统, 4:工具)
     * @param content         消息内容
     * @param messageType     消息类型 (1:文本, 2:图片, 3:文件, 4:工具调用, 5:工具结果)
     * @param status          消息状态 (1:正常, 2:发送中, 3:发送失败, 4:已撤回)
     * @param parentMessageId 父消息ID
     * @param toolCallId      工具调用ID
     * @param toolName        工具名称
     * @return MessageVO对象
     */
    private MessageVO createMessageVO(Long id, Long sessionId, Integer role, String content, Integer messageType,
                                      Integer status, Long parentMessageId, String toolCallId, String toolName) {
        MessageVO message = new MessageVO();
        message.setId(id);
        message.setUserId(10001L);
        message.setLearningSpaceId(20001L);
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setStatus(status);
        message.setParentMessageId(parentMessageId);
        message.setToolCallId(toolCallId);
        message.setToolName(toolName);
        message.setCreatedAt(NOW.minusMinutes((id - 4000) * 5));
        message.setUpdatedAt(NOW.minusMinutes((id - 4000) * 5));
        return message;
    }

    /**
     * 保存JSON数据到文件
     *
     * @param filename 文件名
     * @param json     JSON字符串
     * @throws IOException IO异常
     */
    private void saveToFile(String filename, String json) throws IOException {
        Path outputDir = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        Path filePath = outputDir.resolve(filename);
        Files.write(filePath, json.getBytes("UTF-8"));
    }
}
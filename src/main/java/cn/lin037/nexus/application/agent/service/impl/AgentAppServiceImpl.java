package cn.lin037.nexus.application.agent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.agent.port.AgentChatSessionRepository;
import cn.lin037.nexus.application.agent.port.AgentLearningTaskRepository;
import cn.lin037.nexus.application.agent.port.AgentMemoryRepository;
import cn.lin037.nexus.application.agent.service.AgentAppService;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatSessionStatusEnum;
import cn.lin037.nexus.web.rest.v1.agent.req.*;
import cn.lin037.nexus.web.rest.v1.agent.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Agent应用服务实现
 *
 * @author Lin037
 */
@Service
public class AgentAppServiceImpl implements AgentAppService {

    private final AgentChatSessionRepository sessionRepository;
    private final AgentMemoryRepository memoryRepository;
    private final AgentLearningTaskRepository learningTaskRepository;

    public AgentAppServiceImpl(AgentChatSessionRepository sessionRepository,
                               AgentMemoryRepository memoryRepository,
                               AgentLearningTaskRepository learningTaskRepository) {
        this.sessionRepository = sessionRepository;
        this.memoryRepository = memoryRepository;
        this.learningTaskRepository = learningTaskRepository;
    }

    // ==================== Session相关方法 ====================

    @Override
    @Transactional
    public SessionVO createSession(CreateSessionReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建会话实体
        AgentChatSessionEntity entity = new AgentChatSessionEntity();
        entity.setAcsId(HutoolSnowflakeIdGenerator.generateLongId());
        entity.setAcsUserId(userId);
        entity.setAcsLearningSpaceId(req.getLearningSpaceId());
        entity.setAcsTitle(req.getTitle());
        entity.setAcsType(req.getType());
        // 默认正常状态
        entity.setAcsStatus(AgentChatSessionStatusEnum.NORMAL.getCode());
        entity.setAcsBelongsTo(req.getBelongsTo());
        entity.setAcsIsAutoCallTool(req.getIsAutoCallTool());
        entity.setAcsCreatedAt(LocalDateTime.now());
        entity.setAcsUpdatedAt(LocalDateTime.now());

        // 保存会话
        sessionRepository.save(entity);

        // 返回VO
        return SessionVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public SessionVO updateSession(Long id, UpdateSessionReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建更新实体
        AgentChatSessionEntity entity = new AgentChatSessionEntity();
        entity.setAcsId(id);
        entity.setAcsUserId(userId);
        
        // 更新字段
        if (req.getTitle() != null) {
            entity.setAcsTitle(req.getTitle());
        }
        if (req.getIsAutoCallTool() != null) {
            entity.setAcsIsAutoCallTool(req.getIsAutoCallTool());
        }
        entity.setAcsUpdatedAt(LocalDateTime.now());

        // 保存更新
        sessionRepository.save(entity);

        // 返回VO
        return SessionVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public void deleteSession(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 删除会话
        boolean deleted = sessionRepository.deleteById(id);
        if (!deleted) {
            // 如果删除失败，可以记录日志或抛出异常（根据业务需求）
            // 这里由于是逻辑删除，且删除不存在的记录或已删除的记录返回false，所以不抛出异常
        }
    }

    // ==================== Memory相关方法 ====================

    @Override
    @Transactional
    public MemoryVO createMemory(CreateMemoryReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建记忆实体
        AgentMemoryEntity entity = new AgentMemoryEntity();
        entity.setAmId(HutoolSnowflakeIdGenerator.generateLongId());
        entity.setAmUserId(userId);
        entity.setAmLearningSpaceId(req.getLearningSpaceId());
        entity.setAmSessionId(req.getSessionId());
        entity.setAmLevel(req.getLevel());
        entity.setAmTitle(req.getTitle());
        entity.setAmContent(req.getContent());
        entity.setAmImportanceScore(req.getImportanceScore());
        entity.setAmTags(req.getTags());
        entity.setAmSource(req.getSource());
        entity.setAmAccessCount(0);
        entity.setAmLastAccessedAt(LocalDateTime.now());
        entity.setAmCreatedAt(LocalDateTime.now());
        entity.setAmUpdatedAt(LocalDateTime.now());

        // 保存记忆
        memoryRepository.save(entity);

        // 返回VO
        return MemoryVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public MemoryVO updateMemory(Long id, UpdateMemoryReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建更新实体
        AgentMemoryEntity entity = new AgentMemoryEntity();
        entity.setAmId(id);
        entity.setAmUserId(userId);

        // 更新字段
        if (req.getTitle() != null) {
            entity.setAmTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            entity.setAmContent(req.getContent());
        }
        if (req.getImportanceScore() != null) {
            entity.setAmImportanceScore(req.getImportanceScore());
        }
        if (req.getTags() != null) {
            entity.setAmTags(req.getTags());
        }
        entity.setAmUpdatedAt(LocalDateTime.now());

        // 保存更新
        memoryRepository.save(entity);

        // 返回VO
        return MemoryVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public void deleteMemory(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 删除记忆
        memoryRepository.deleteById(id);
    }


    // ==================== LearningTask相关方法 ====================

    @Override
    @Transactional
    public LearningTaskVO createLearningTask(CreateLearningTaskReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建学习任务实体
        AgentLearningTaskEntity entity = new AgentLearningTaskEntity();
        entity.setAltId(HutoolSnowflakeIdGenerator.generateLongId());
        entity.setAltUserId(userId);
        entity.setAltLearningSpaceId(req.getLearningSpaceId());
        entity.setAltSessionId(req.getSessionId());
        entity.setAltTitle(req.getTitle());
        entity.setAltObjective(req.getObjective());
        entity.setAltDifficultyLevel(req.getDifficultyLevel().getCode());
        entity.setAltIsCompleted(false);
        entity.setAltCreatedAt(LocalDateTime.now());
        entity.setAltUpdatedAt(LocalDateTime.now());

        // 保存学习任务
        learningTaskRepository.save(entity);

        // 返回VO
        return LearningTaskVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public LearningTaskVO updateLearningTask(Long id, UpdateLearningTaskReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建更新实体
        AgentLearningTaskEntity entity = new AgentLearningTaskEntity();
        entity.setAltId(id);
        entity.setAltUserId(userId);

        // 更新字段
        if (req.getTitle() != null) {
            entity.setAltTitle(req.getTitle());
        }
        if (req.getObjective() != null) {
            entity.setAltObjective(req.getObjective());
        }
        if (req.getDifficultyLevel() != null) {
            entity.setAltDifficultyLevel(req.getDifficultyLevel().getCode());
        }
        if (req.getIsCompleted() != null) {
            entity.setAltIsCompleted(req.getIsCompleted());
        }
        entity.setAltUpdatedAt(LocalDateTime.now());

        // 保存更新
        learningTaskRepository.save(entity);

        // 返回VO
        return LearningTaskVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public void deleteLearningTask(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 删除学习任务
        learningTaskRepository.deleteById(id);
    }

}

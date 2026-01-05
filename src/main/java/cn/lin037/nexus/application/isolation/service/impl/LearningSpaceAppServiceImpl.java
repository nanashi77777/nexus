package cn.lin037.nexus.application.isolation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.isolation.port.LearningSpaceRepository;
import cn.lin037.nexus.application.isolation.service.LearningSpaceAppService;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.LearningSpaceEntity;
import cn.lin037.nexus.web.rest.v1.isolation.req.CreateLearningSpaceReq;
import cn.lin037.nexus.web.rest.v1.isolation.req.UpdateLearningSpaceReq;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author GitHub Copilot
 */
@Service
public class LearningSpaceAppServiceImpl implements LearningSpaceAppService {

    private final LearningSpaceRepository learningSpaceRepository;

    public LearningSpaceAppServiceImpl(LearningSpaceRepository learningSpaceRepository) {
        this.learningSpaceRepository = learningSpaceRepository;
    }

    @Override
    public Long createLearningSpace(CreateLearningSpaceReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        LearningSpaceEntity learningSpaceEntity = new LearningSpaceEntity();
        learningSpaceEntity.setLsId(HutoolSnowflakeIdGenerator.generateLongId());
        learningSpaceEntity.setLsUserId(userId);
        learningSpaceEntity.setLsName(req.getName());
        learningSpaceEntity.setLsDescription(req.getDescription());
        learningSpaceEntity.setLsSpacePrompt(req.getSpacePrompt());
        learningSpaceEntity.setLsCoverImageUrl(req.getCoverImageUrl());
        learningSpaceEntity.setLsCreatedAt(LocalDateTime.now());
        learningSpaceEntity.setLsUpdatedAt(LocalDateTime.now());
        learningSpaceRepository.save(learningSpaceEntity);
        return learningSpaceEntity.getLsId();
    }

    @Override
    public void updateLearningSpace(Long id, UpdateLearningSpaceReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        // First, verify the learning space belongs to the user.
        learningSpaceRepository.findById(id, null).ifPresent(space -> {
            if (!space.getLsUserId().equals(userId)) {
                // Or throw an exception
                return;
            }
            learningSpaceRepository.updateById(id, entity -> {
                if (req.getName() != null) {
                    entity.setLsName(req.getName());
                }
                if (req.getDescription() != null) {
                    entity.setLsDescription(req.getDescription());
                }
                if (req.getSpacePrompt() != null) {
                    entity.setLsSpacePrompt(req.getSpacePrompt());
                }
                if (req.getCoverImageUrl() != null) {
                    entity.setLsCoverImageUrl(req.getCoverImageUrl());
                }
                entity.setLsUpdatedAt(LocalDateTime.now());
            });
        });
    }

    @Override
    public void deleteLearningSpace(Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        // First, verify the learning space belongs to the user.
        learningSpaceRepository.findById(id, null).ifPresent(space -> {
            if (!space.getLsUserId().equals(userId)) {
                // Or throw an exception
                return;
            }
            learningSpaceRepository.deleteById(id);
        });
    }
}


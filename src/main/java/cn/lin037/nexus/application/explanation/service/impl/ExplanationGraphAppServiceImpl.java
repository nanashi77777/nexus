package cn.lin037.nexus.application.explanation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.application.explanation.enums.ExplanationErrorCodeEnum;
import cn.lin037.nexus.application.explanation.port.ExplanationPointRepository;
import cn.lin037.nexus.application.explanation.port.ExplanationRelationRepository;
import cn.lin037.nexus.application.explanation.service.ExplanationGraphAppService;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationPointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationRelationEntity;
import cn.lin037.nexus.web.rest.v1.explanation.req.*;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationPointVO;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationRelationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 讲解图谱应用服务实现
 *
 * @author LinSanQi
 */
@Service
@RequiredArgsConstructor
public class ExplanationGraphAppServiceImpl implements ExplanationGraphAppService {

    private final ExplanationPointRepository explanationPointRepository;
    private final ExplanationRelationRepository explanationRelationRepository;

    @Override
    @Transactional
    public void updateStyles(BatchUpdateStyleReq req) {
        // 批量更新样式配置
        // 这里可以根据具体需求实现批量样式更新逻辑
    }

    @Override
    @Transactional
    public ExplanationPointVO createPoint(CreateExplanationPointReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 构建知识点实体
        ExplanationPointEntity point = new ExplanationPointEntity();
        point.setEpId(HutoolSnowflakeIdGenerator.generateLongId());
        point.setEpExplanationDocumentId(req.getExplanationDocumentId());
        point.setEpTitle(req.getTitle());
        point.setEpDefinition(req.getDefinition());
        point.setEpExplanation(req.getExplanation());
        point.setEpFormulaOrCode(req.getFormulaOrCode());
        point.setEpExample(req.getExample());
        point.setEpStyleConfig(req.getStyleConfig() != null ? JSONUtil.toJsonStr(req.getStyleConfig()) : null);
        point.setEpCreatedByUserId(currentUserId);
        point.setEpCreatedAt(LocalDateTime.now());
        point.setEpUpdatedAt(LocalDateTime.now());

        point = explanationPointRepository.save(point);
        return convertPointToVO(point);
    }

    @Override
    @Transactional
    public ExplanationPointVO updatePoint(Long pointId, UpdateExplanationPointReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ExplanationPointEntity point = explanationPointRepository.findById(pointId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_POINT_NOT_FOUND));

        if (!point.getEpCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_POINT_ACCESS_DENIED);
        }

        // 更新知识点信息
        point.setEpTitle(req.getTitle());
        point.setEpDefinition(req.getDefinition());
        point.setEpExplanation(req.getExplanation());
        point.setEpFormulaOrCode(req.getFormulaOrCode());
        point.setEpExample(req.getExample());
        point.setEpStyleConfig(req.getStyleConfig() != null ? JSONUtil.toJsonStr(req.getStyleConfig()) : null);
        point.setEpUpdatedAt(LocalDateTime.now());

        point = explanationPointRepository.save(point);
        return convertPointToVO(point);
    }

    @Override
    @Transactional
    public void deletePoint(Long pointId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ExplanationPointEntity point = explanationPointRepository.findById(pointId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_POINT_NOT_FOUND));

        if (!point.getEpCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_POINT_ACCESS_DENIED);
        }

        // 逻辑删除知识点
        point.setEpDeletedAt(LocalDateTime.now());
        explanationPointRepository.save(point);
    }

    @Override
    @Transactional
    public ExplanationRelationVO createRelation(CreateExplanationRelationReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 构建关系实体
        ExplanationRelationEntity relation = new ExplanationRelationEntity();
        relation.setErId(HutoolSnowflakeIdGenerator.generateLongId());
        relation.setErExplanationDocumentId(req.getExplanationDocumentId());
        relation.setErSourcePointId(req.getSourcePointId());
        relation.setErTargetPointId(req.getTargetPointId());
        relation.setErRelationType(req.getRelationType());
        relation.setErDescription(req.getDescription());
        relation.setErStyleConfig(req.getStyleConfig() != null ? JSONUtil.toJsonStr(req.getStyleConfig()) : null);
        relation.setErCreatedByUserId(currentUserId);
        relation.setErCreatedAt(LocalDateTime.now());
        relation.setErUpdatedAt(LocalDateTime.now());

        relation = explanationRelationRepository.save(relation);
        return convertRelationToVO(relation);
    }

    @Override
    @Transactional
    public ExplanationRelationVO updateRelation(Long relationId, UpdateExplanationRelationReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ExplanationRelationEntity relation = explanationRelationRepository.findById(relationId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_RELATION_NOT_FOUND));

        if (!relation.getErCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_RELATION_ACCESS_DENIED);
        }

        // 更新关系信息
        relation.setErRelationType(req.getRelationType());
        relation.setErDescription(req.getDescription());
        relation.setErStyleConfig(req.getStyleConfig() != null ? JSONUtil.toJsonStr(req.getStyleConfig()) : null);
        relation.setErUpdatedAt(LocalDateTime.now());

        relation = explanationRelationRepository.save(relation);
        return convertRelationToVO(relation);
    }

    @Override
    @Transactional
    public void deleteRelation(Long relationId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ExplanationRelationEntity relation = explanationRelationRepository.findById(relationId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_RELATION_NOT_FOUND));

        if (!relation.getErCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_RELATION_ACCESS_DENIED);
        }

        // 逻辑删除关系
        relation.setErDeletedAt(LocalDateTime.now());
        explanationRelationRepository.save(relation);
    }

    /**
     * 转换知识点为VO
     */
    private ExplanationPointVO convertPointToVO(ExplanationPointEntity entity) {
        ExplanationPointVO vo = new ExplanationPointVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 转换关系为VO
     */
    private ExplanationRelationVO convertRelationToVO(ExplanationRelationEntity entity) {
        ExplanationRelationVO vo = new ExplanationRelationVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }
}

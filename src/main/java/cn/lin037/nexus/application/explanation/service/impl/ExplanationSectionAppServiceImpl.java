package cn.lin037.nexus.application.explanation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.application.explanation.enums.ExplanationErrorCodeEnum;
import cn.lin037.nexus.application.explanation.port.ExplanationDocumentRepository;
import cn.lin037.nexus.application.explanation.port.ExplanationSectionRepository;
import cn.lin037.nexus.application.explanation.port.ExplanationSubsectionRepository;
import cn.lin037.nexus.application.explanation.service.ExplanationSectionAppService;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationDocumentEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSectionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSubsectionEntity;
import cn.lin037.nexus.web.rest.v1.explanation.req.AdjustSectionPositionReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.CreateExplanationSectionReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.UpdateExplanationSectionReq;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationSectionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 讲解章节应用服务实现
 *
 * @author LinSanQi
 */
@Service
@RequiredArgsConstructor
public class ExplanationSectionAppServiceImpl implements ExplanationSectionAppService {

    private final ExplanationSectionRepository explanationSectionRepository;
    private final ExplanationSubsectionRepository explanationSubsectionRepository;
    private final ExplanationDocumentRepository explanationDocumentRepository;

    @Override
    @Transactional
    public ExplanationSectionVO createSection(CreateExplanationSectionReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 验证讲解文档是否存在且属于当前用户
        explanationDocumentRepository.findByIdAndUserId(req.getExplanationDocumentId(), currentUserId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_NOT_FOUND));

        // 构建章节实体
        ExplanationSectionEntity section = new ExplanationSectionEntity();
        section.setEsId(HutoolSnowflakeIdGenerator.generateLongId());
        section.setEsExplanationDocumentId(req.getExplanationDocumentId());
        section.setEsTitle(req.getTitle());
        section.setEsSummary(req.getSummary());
        section.setEsContent(req.getContent());
        section.setEsCreatedByUserId(currentUserId);
        section.setEsCreatedAt(LocalDateTime.now());
        section.setEsUpdatedAt(LocalDateTime.now());

        section = explanationSectionRepository.save(section);
        return convertToVO(section);
    }

    @Override
    @Transactional
    public ExplanationSectionVO updateSection(Long sectionId, UpdateExplanationSectionReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ExplanationSectionEntity section = explanationSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_NOT_FOUND));

        if (!section.getEsCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_ACCESS_DENIED);
        }

        // 更新章节信息
        section.setEsTitle(req.getTitle());
        section.setEsSummary(req.getSummary());
        section.setEsContent(req.getContent());
        section.setEsUpdatedAt(LocalDateTime.now());

        section = explanationSectionRepository.save(section);
        return convertToVO(section);
    }

    @Override
    @Transactional
    public void deleteSection(Long sectionId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ExplanationSectionEntity section = explanationSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_NOT_FOUND));

        if (!section.getEsCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_ACCESS_DENIED);
        }

        // 级联删除所有小节
        List<ExplanationSubsectionEntity> subsections = explanationSubsectionRepository.findBySectionId(sectionId);
        for (ExplanationSubsectionEntity subsection : subsections) {
            subsection.setEssDeletedAt(LocalDateTime.now());
            explanationSubsectionRepository.save(subsection);
        }

        // 删除章节
        section.setEsDeletedAt(LocalDateTime.now());
        explanationSectionRepository.save(section);
    }

    @Override
    @Transactional
    public void adjustPositions(AdjustSectionPositionReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 验证文档权限
        ExplanationDocumentEntity document = explanationDocumentRepository.findByIdAndUserId(req.getDocumentId(), currentUserId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_NOT_FOUND));

        if (req.getAdjustmentType() == AdjustSectionPositionReq.AdjustmentType.SECTION) {
            // 调整章节位置顺序
            adjustSectionOrder(document, req.getNewSectionOrder(), currentUserId);
        } else if (req.getAdjustmentType() == AdjustSectionPositionReq.AdjustmentType.SUBSECTION) {
            // 调整小节位置顺序
            adjustSubsectionOrder(req.getSectionId(), req.getNewSubsectionOrder(), currentUserId);
        } else {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_ORDER_INVALID, "不支持的调整类型");
        }
    }

    /**
     * 调整章节位置顺序
     */
    private void adjustSectionOrder(ExplanationDocumentEntity document, List<Long> newSectionOrder, Long userId) {
        if (newSectionOrder == null || newSectionOrder.isEmpty()) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_ORDER_INVALID, "新的章节顺序不能为空");
        }

        // 验证所有章节都属于当前文档和用户 TODO: 需要优化
        List<ExplanationSectionEntity> existingSections = explanationSectionRepository.findByExplanationDocumentId(document.getEdId());
        Set<Long> existingSectionIds = existingSections.stream()
                .filter(section -> section.getEsCreatedByUserId().equals(userId))
                .map(ExplanationSectionEntity::getEsId)
                .collect(Collectors.toSet());

        for (Long sectionId : newSectionOrder) {
            if (!existingSectionIds.contains(sectionId)) {
                throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_NOT_FOUND,
                        "章节 " + sectionId + " 不存在或无权限操作");
            }
        }

        // 更新文档的章节顺序
        document.setEdSectionOrder(newSectionOrder);
        document.setEdUpdatedAt(LocalDateTime.now());

        explanationDocumentRepository.updateById(document);
    }

    /**
     * 调整小节位置顺序
     */
    private void adjustSubsectionOrder(Long sectionId, List<Long> newSubsectionOrder, Long userId) {
        if (sectionId == null) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_NOT_FOUND, "章节ID不能为空");
        }

        if (newSubsectionOrder == null || newSubsectionOrder.isEmpty()) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SUBSECTION_ORDER_INVALID, "新的小节顺序不能为空");
        }

        // 获取章节并验证权限
        ExplanationSectionEntity section = explanationSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_NOT_FOUND));

        if (!section.getEsCreatedByUserId().equals(userId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_ACCESS_DENIED);
        }

        // 验证所有小节都属于当前章节和用户
        List<ExplanationSubsectionEntity> existingSubsections = explanationSubsectionRepository.findBySectionId(sectionId);
        Set<Long> existingSubsectionIds = existingSubsections.stream()
                .filter(subsection -> subsection.getEssCreatedByUserId().equals(userId))
                .map(ExplanationSubsectionEntity::getEssId)
                .collect(Collectors.toSet());

        for (Long subsectionId : newSubsectionOrder) {
            if (!existingSubsectionIds.contains(subsectionId)) {
                throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SUBSECTION_NOT_FOUND,
                        "小节 " + subsectionId + " 不存在或无权限操作");
            }
        }

        // 更新章节的小节顺序 TODO: 需要优化
        section.setEsSubsectionOrder(newSubsectionOrder);
        section.setEsUpdatedAt(LocalDateTime.now());

        explanationSectionRepository.save(section);
    }

    @Override
    public List<ExplanationSectionVO> listSectionsByDocument(Long explanationDocumentId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 验证文档权限
        explanationDocumentRepository.findByIdAndUserId(explanationDocumentId, currentUserId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_NOT_FOUND));

        List<ExplanationSectionEntity> sections = explanationSectionRepository.findByExplanationDocumentId(explanationDocumentId);
        return sections.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private ExplanationSectionVO convertToVO(ExplanationSectionEntity entity) {
        ExplanationSectionVO vo = new ExplanationSectionVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }
}

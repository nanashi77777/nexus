package cn.lin037.nexus.application.explanation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.application.explanation.enums.ExplanationErrorCodeEnum;
import cn.lin037.nexus.application.explanation.port.ExplanationSectionRepository;
import cn.lin037.nexus.application.explanation.port.ExplanationSubsectionRepository;
import cn.lin037.nexus.application.explanation.service.ExplanationSubsectionAppService;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSectionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSubsectionEntity;
import cn.lin037.nexus.web.rest.v1.explanation.req.CreateExplanationSubsectionReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.UpdateExplanationSubsectionReq;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationSubsectionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 讲解小节应用服务实现
 *
 * @author LinSanQi
 */
@Service
@RequiredArgsConstructor
public class ExplanationSubsectionAppServiceImpl implements ExplanationSubsectionAppService {

    private final ExplanationSubsectionRepository explanationSubsectionRepository;
    private final ExplanationSectionRepository explanationSectionRepository;

    @Override
    @Transactional
    public ExplanationSubsectionVO createSubsection(Long sectionId, CreateExplanationSubsectionReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 验证章节是否存在且属于当前用户
        ExplanationSectionEntity section = explanationSectionRepository.findById(sectionId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_NOT_FOUND));

        if (!section.getEsCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SECTION_ACCESS_DENIED);
        }

        // 构建小节实体
        ExplanationSubsectionEntity subsection = new ExplanationSubsectionEntity();
        subsection.setEssId(HutoolSnowflakeIdGenerator.generateLongId());
        subsection.setEssSectionId(sectionId);
        subsection.setEssTitle(req.getTitle());
        subsection.setEssSummary(req.getSummary());
        subsection.setEssContent(req.getContent());
        subsection.setEssCreatedByUserId(currentUserId);
        subsection.setEssCreatedAt(LocalDateTime.now());
        subsection.setEssUpdatedAt(LocalDateTime.now());

        subsection = explanationSubsectionRepository.save(subsection);
        return convertToVO(subsection);
    }

    @Override
    @Transactional
    public ExplanationSubsectionVO updateSubsection(Long subsectionId, UpdateExplanationSubsectionReq req) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ExplanationSubsectionEntity subsection = explanationSubsectionRepository.findById(subsectionId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SUBSECTION_NOT_FOUND));

        if (!subsection.getEssCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SUBSECTION_ACCESS_DENIED);
        }

        // 更新小节信息
        subsection.setEssTitle(req.getTitle());
        subsection.setEssSummary(req.getSummary());
        subsection.setEssContent(req.getContent());
        subsection.setEssUpdatedAt(LocalDateTime.now());

        subsection = explanationSubsectionRepository.save(subsection);
        return convertToVO(subsection);
    }

    @Override
    @Transactional
    public void deleteSubsection(Long subsectionId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ExplanationSubsectionEntity subsection = explanationSubsectionRepository.findById(subsectionId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SUBSECTION_NOT_FOUND));

        if (!subsection.getEssCreatedByUserId().equals(currentUserId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_SUBSECTION_ACCESS_DENIED);
        }

        // 逻辑删除小节
        subsection.setEssDeletedAt(LocalDateTime.now());
        explanationSubsectionRepository.save(subsection);
    }

    /**
     * 转换为VO
     */
    private ExplanationSubsectionVO convertToVO(ExplanationSubsectionEntity entity) {
        ExplanationSubsectionVO vo = new ExplanationSubsectionVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }
}

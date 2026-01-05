package cn.lin037.nexus.application.explanation.service;

import cn.lin037.nexus.web.rest.v1.explanation.req.AdjustSectionPositionReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.CreateExplanationSectionReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.UpdateExplanationSectionReq;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationSectionVO;

import java.util.List;

/**
 * 章节应用服务
 *
 * @author LinSanQi
 */
public interface ExplanationSectionAppService {

    /**
     * 创建章节
     *
     * @param req 创建请求
     * @return 新创建的章节
     */
    ExplanationSectionVO createSection(CreateExplanationSectionReq req);

    /**
     * 更新章节
     *
     * @param sectionId 章节ID
     * @param req       更新请求
     * @return 更新后的章节
     */
    ExplanationSectionVO updateSection(Long sectionId, UpdateExplanationSectionReq req);

    /**
     * 删除章节
     *
     * @param sectionId 章节ID
     */
    void deleteSection(Long sectionId);

    /**
     * 调整位置关系
     *
     * @param req 位置调整请求
     */
    void adjustPositions(AdjustSectionPositionReq req);

    /**
     * 根据文档ID列出章节
     *
     * @param explanationDocumentId 讲解文档ID
     * @return 章节列表
     */
    List<ExplanationSectionVO> listSectionsByDocument(Long explanationDocumentId);
}

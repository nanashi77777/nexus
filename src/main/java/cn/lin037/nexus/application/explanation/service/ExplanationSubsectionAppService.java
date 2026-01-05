package cn.lin037.nexus.application.explanation.service;

import cn.lin037.nexus.web.rest.v1.explanation.req.CreateExplanationSubsectionReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.UpdateExplanationSubsectionReq;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationSubsectionVO;

/**
 * 小节应用服务
 *
 * @author LinSanQi
 */
public interface ExplanationSubsectionAppService {

    /**
     * 创建小节
     *
     * @param sectionId 章节ID
     * @param req       创建请求
     * @return 新创建的小节
     */
    ExplanationSubsectionVO createSubsection(Long sectionId, CreateExplanationSubsectionReq req);

    /**
     * 更新小节
     *
     * @param subsectionId 小节ID
     * @param req          更新请求
     * @return 更新后的小节
     */
    ExplanationSubsectionVO updateSubsection(Long subsectionId, UpdateExplanationSubsectionReq req);

    /**
     * 删除小节
     *
     * @param subsectionId 小节ID
     */
    void deleteSubsection(Long subsectionId);
}

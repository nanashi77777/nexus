package cn.lin037.nexus.application.explanation.service;

import cn.lin037.nexus.web.rest.v1.explanation.req.AiGenerateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.CreateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.UpdateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationDocumentVO;


/**
 * 讲解文档应用服务
 *
 * @author LinSanQi
 */
public interface ExplanationDocumentAppService {

    /**
     * 创建讲解文档
     *
     * @param req 创建请求
     * @return 新创建的讲解文档
     */
    ExplanationDocumentVO createExplanationDocument(CreateExplanationDocumentReq req);

    /**
     * 更新讲解文档
     *
     * @param documentId 讲解文档ID
     * @param req        更新请求
     * @return 更新后的讲解文档
     */
    ExplanationDocumentVO updateExplanationDocument(Long documentId, UpdateExplanationDocumentReq req);

    /**
     * 删除讲解文档（级联删除）
     *
     * @param documentId 讲解文档ID
     */
    void deleteExplanationDocument(Long documentId);

    /**
     * AI生成讲解文档
     *
     * @param req AI生成请求
     * @return 任务ID
     */
    Long aiGenerateExplanationDocument(AiGenerateExplanationDocumentReq req);
}

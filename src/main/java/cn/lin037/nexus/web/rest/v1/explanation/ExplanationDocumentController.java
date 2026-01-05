package cn.lin037.nexus.web.rest.v1.explanation;

import cn.lin037.nexus.application.explanation.service.ExplanationDocumentAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.explanation.req.AiGenerateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.CreateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.UpdateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationDocumentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 讲解文档控制器
 *
 * @author LinSanQi
 */
@RestController
@RequestMapping("/api/v1/explanation/documents")
public class ExplanationDocumentController {

    private final ExplanationDocumentAppService explanationDocumentAppService;

    public ExplanationDocumentController(ExplanationDocumentAppService explanationDocumentAppService) {
        this.explanationDocumentAppService = explanationDocumentAppService;
    }

    /**
     * 手动创建讲解文档
     * COMPLETED
     *
     * @param req 创建请求
     * @return 新创建的讲解文档
     */
    @PostMapping
    public ResultVO<ExplanationDocumentVO> createExplanationDocument(@Valid @RequestBody CreateExplanationDocumentReq req) {
        ExplanationDocumentVO document = explanationDocumentAppService.createExplanationDocument(req);
        return ResultVO.success(document);
    }

    /**
     * 修改讲解文档的基本信息
     * COMPLETED
     *
     * @param documentId 讲解文档ID
     * @param req        修改请求
     * @return 修改后的讲解文档
     */
    @PutMapping("/{documentId}")
    public ResultVO<ExplanationDocumentVO> updateExplanationDocument(@PathVariable Long documentId, @Valid @RequestBody UpdateExplanationDocumentReq req) {
        ExplanationDocumentVO document = explanationDocumentAppService.updateExplanationDocument(documentId, req);
        return ResultVO.success(document);
    }

    /**
     * 删除讲解文档（级联删除）
     * COMPLETED
     *
     * @param documentId 讲解文档ID
     * @return 删除成功
     */
    @DeleteMapping("/{documentId}")
    public ResultVO<Void> deleteExplanationDocument(@PathVariable Long documentId) {
        explanationDocumentAppService.deleteExplanationDocument(documentId);
        return ResultVO.success();
    }

    /**
     * AI生成讲解文档
     *
     * @param req AI生成请求
     * @return 任务ID
     */
    @PostMapping("/ai-generate")
    public ResultVO<Long> aiGenerateExplanationDocument(@Valid @RequestBody AiGenerateExplanationDocumentReq req) {
        Long taskId = explanationDocumentAppService.aiGenerateExplanationDocument(req);
        return ResultVO.success(taskId);
    }
}

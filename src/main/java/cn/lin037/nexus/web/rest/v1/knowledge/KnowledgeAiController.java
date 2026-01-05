package cn.lin037.nexus.web.rest.v1.knowledge;

import cn.lin037.nexus.application.knowledge.service.KnowledgeAiAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.knowledge.req.AiConnectKnowledgeReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.AiExpandKnowledgeReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.AiGenerateKnowledgeFromResourceReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识模块AI能力接口
 *
 * @author LinSanQi
 */
@Tag(name = "Knowledge AI Controller", description = "知识模块AI能力接口")
@RestController
@RequestMapping("/api/v1/knowledge-ai")
@RequiredArgsConstructor
public class KnowledgeAiController {

    private final KnowledgeAiAppService knowledgeAiAppService;

    @Operation(summary = "从资源中根据用户Prompt生成知识点和知识点关系", description = "从指定的文档或分片中提取知识点，并存入文件夹或知识图谱")
    @PostMapping("/generate-knowledge")
    public ResultVO<Long> generateFromResource(@Valid @RequestBody AiGenerateKnowledgeFromResourceReq req) {
        Long taskId = knowledgeAiAppService.generateFromResources(req);
        return ResultVO.success(taskId);
    }

    @Operation(summary = "拓展已有知识点", description = "根据已有知识点和用户Prompt生成更多相关知识点")
    @PostMapping("/expand")
    public ResultVO<Long> expandKnowledge(@Valid @RequestBody AiExpandKnowledgeReq req) {
        Long taskId = knowledgeAiAppService.expandKnowledge(req);
        return ResultVO.success(taskId);
    }

    @Operation(summary = "为已有知识点生成关联关系", description = "根据给定的知识点，AI尝试为它们创建合乎逻辑的关联")
    @PostMapping("/connect")
    public ResultVO<Long> connectKnowledge(@Valid @RequestBody AiConnectKnowledgeReq req) {
        Long taskId = knowledgeAiAppService.connectKnowledge(req);
        return ResultVO.success(taskId);
    }
}

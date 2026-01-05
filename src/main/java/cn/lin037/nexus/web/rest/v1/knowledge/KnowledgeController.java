package cn.lin037.nexus.web.rest.v1.knowledge;

import cn.lin037.nexus.application.knowledge.service.KnowledgeAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateKnowledgePointRelationReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateKnowledgePointReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.UpdateKnowledgePointRelationReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.UpdateKnowledgePointReq;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgePointRelationVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgePointVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeAppService knowledgeAppService;

    public KnowledgeController(KnowledgeAppService knowledgeAppService) {
        this.knowledgeAppService = knowledgeAppService;
    }

    /**
     * 创建知识点
     * COMPLETED
     *
     * @param req 创建知识点请求
     * @return 新创建的知识点
     */
    @PostMapping("/points")
    public ResultVO<KnowledgePointVO> createKnowledgePoint(@Valid @RequestBody CreateKnowledgePointReq req) {
        KnowledgePointVO point = knowledgeAppService.createKnowledgePoint(req);
        return ResultVO.success(point);
    }

    /**
     * 更新知识点
     * COMPLETED
     *
     * @param pointId 知识点ID
     * @param req     更新知识点请求
     * @return 更新后的知识点
     */
    @PutMapping("/points/{pointId}")
    public ResultVO<KnowledgePointVO> updateKnowledgePoint(@PathVariable Long pointId, @Valid @RequestBody UpdateKnowledgePointReq req) {
        KnowledgePointVO point = knowledgeAppService.updateKnowledgePoint(pointId, req);
        return ResultVO.success(point);
    }

    /**
     * 回溯知识点版本
     * COMPLETED
     *
     * @param pointId   知识点ID
     * @param versionId 版本ID
     * @return 回溯后的知识点
     */
    @PostMapping("/points/{pointId}/revert")
    public ResultVO<KnowledgePointVO> revertKnowledgePointToVersion(@PathVariable Long pointId, @RequestParam Long versionId) {
        KnowledgePointVO point = knowledgeAppService.revertToVersion(pointId, versionId);
        return ResultVO.success(point);
    }

    /**
     * 删除知识点
     * COMPLETED
     *
     * @param pointId 知识点ID
     * @return 删除成功
     */
    @DeleteMapping("/points/{pointId}")
    public ResultVO<Void> deleteKnowledgePoint(@PathVariable Long pointId) {
        knowledgeAppService.deleteKnowledgePoint(pointId);
        return ResultVO.success();
    }

    /**
     * 创建知识点关系
     * COMPLETED
     *
     * @param req 创建请求
     * @return 创建的关系信息
     */
    @PostMapping("/relations")
    public ResultVO<KnowledgePointRelationVO> createKnowledgePointRelation(@Valid @RequestBody CreateKnowledgePointRelationReq req) {
        KnowledgePointRelationVO relation = knowledgeAppService.createKnowledgePointRelation(req);
        return ResultVO.success(relation);
    }

    /**
     * 更新知识点关系
     * COMPLETED
     *
     * @param relationId 关系ID
     * @param req        更新请求
     * @return 更新后的关系信息
     */
    @PutMapping("/relations/{relationId}")
    public ResultVO<KnowledgePointRelationVO> updateKnowledgePointRelation(@PathVariable Long relationId, @Valid @RequestBody UpdateKnowledgePointRelationReq req) {
        KnowledgePointRelationVO relation = knowledgeAppService.updateKnowledgePointRelation(relationId, req);
        return ResultVO.success(relation);
    }

    /**
     * 删除知识点关系
     * COMPLETED
     *
     * @param relationId 关系ID
     * @return 删除成功
     */
    @DeleteMapping("/relations/{relationId}")
    public ResultVO<Void> deleteKnowledgePointRelation(@PathVariable Long relationId) {
        knowledgeAppService.deleteKnowledgePointRelation(relationId);
        return ResultVO.success();
    }

}
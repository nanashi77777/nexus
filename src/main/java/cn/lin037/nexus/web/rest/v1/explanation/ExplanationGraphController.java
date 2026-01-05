package cn.lin037.nexus.web.rest.v1.explanation;

import cn.lin037.nexus.application.explanation.service.ExplanationGraphAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.explanation.req.*;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationPointVO;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationRelationVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 讲解图谱控制器
 *
 * @author LinSanQi
 */
@RestController
@RequestMapping("/api/v1/explanation/graph")
public class ExplanationGraphController {

    private final ExplanationGraphAppService explanationGraphAppService;

    public ExplanationGraphController(ExplanationGraphAppService explanationGraphAppService) {
        this.explanationGraphAppService = explanationGraphAppService;
    }

    /**
     * 批量保存更新样式配置
     *
     * @param req 批量样式更新请求
     * @return 更新成功
     */
    @PutMapping("/styles")
    public ResultVO<Void> updateStyles(@Valid @RequestBody BatchUpdateStyleReq req) {
        explanationGraphAppService.updateStyles(req);
        return ResultVO.success();
    }

    /**
     * 手动新增知识点
     *
     * @param req 创建知识点请求
     * @return 新创建的知识点
     */
    @PostMapping("/points")
    public ResultVO<ExplanationPointVO> createPoint(@Valid @RequestBody CreateExplanationPointReq req) {
        ExplanationPointVO point = explanationGraphAppService.createPoint(req);
        return ResultVO.success(point);
    }

    /**
     * 修改知识点
     *
     * @param pointId 知识点ID
     * @param req     修改请求
     * @return 修改后的知识点
     */
    @PutMapping("/points/{pointId}")
    public ResultVO<ExplanationPointVO> updatePoint(@PathVariable Long pointId, @Valid @RequestBody UpdateExplanationPointReq req) {
        ExplanationPointVO point = explanationGraphAppService.updatePoint(pointId, req);
        return ResultVO.success(point);
    }

    /**
     * 删除知识点（连带关联的边也要删除）
     *
     * @param pointId 知识点ID
     * @return 删除成功
     */
    @DeleteMapping("/points/{pointId}")
    public ResultVO<Void> deletePoint(@PathVariable Long pointId) {
        explanationGraphAppService.deletePoint(pointId);
        return ResultVO.success();
    }

    /**
     * 手动新增关系（将两个知识点进行连接）
     *
     * @param req 创建关系请求
     * @return 新创建的关系
     */
    @PostMapping("/relations")
    public ResultVO<ExplanationRelationVO> createRelation(@Valid @RequestBody CreateExplanationRelationReq req) {
        ExplanationRelationVO relation = explanationGraphAppService.createRelation(req);
        return ResultVO.success(relation);
    }

    /**
     * 修改关系
     *
     * @param relationId 关系ID
     * @param req        修改请求
     * @return 修改后的关系
     */
    @PutMapping("/relations/{relationId}")
    public ResultVO<ExplanationRelationVO> updateRelation(@PathVariable Long relationId, @Valid @RequestBody UpdateExplanationRelationReq req) {
        ExplanationRelationVO relation = explanationGraphAppService.updateRelation(relationId, req);
        return ResultVO.success(relation);
    }

    /**
     * 删除关系（知识点不需要删除）
     *
     * @param relationId 关系ID
     * @return 删除成功
     */
    @DeleteMapping("/relations/{relationId}")
    public ResultVO<Void> deleteRelation(@PathVariable Long relationId) {
        explanationGraphAppService.deleteRelation(relationId);
        return ResultVO.success();
    }
}

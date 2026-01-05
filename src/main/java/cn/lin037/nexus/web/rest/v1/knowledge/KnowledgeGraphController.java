package cn.lin037.nexus.web.rest.v1.knowledge;

import cn.lin037.nexus.application.knowledge.service.KnowledgeGraphAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.knowledge.req.*;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.GraphEdgeVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.GraphNodeVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgeGraphVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识图谱控制器
 * COMPLETED
 *
 * @author LinSanQi
 */
@RestController
@RequestMapping("/api/v1/knowledge/graphs")
public class KnowledgeGraphController {

    private final KnowledgeGraphAppService knowledgeGraphAppService;

    public KnowledgeGraphController(KnowledgeGraphAppService knowledgeGraphAppService) {
        this.knowledgeGraphAppService = knowledgeGraphAppService;
    }

    /**
     * 创建知识图谱
     * COMPLETED
     *
     * @param req 创建请求
     * @return 新创建的知识图谱
     */
    @PostMapping
    public ResultVO<KnowledgeGraphVO> createKnowledgeGraph(@Valid @RequestBody CreateKnowledgeGraphReq req) {
        KnowledgeGraphVO graph = knowledgeGraphAppService.createKnowledgeGraph(req);
        return ResultVO.success(graph);
    }

    /**
     * 更新知识图谱
     * COMPLETED
     *
     * @param graphId 图谱ID
     * @param req     更新请求
     * @return 更新后的图谱信息
     */
    @PutMapping("/{graphId}")
    public ResultVO<KnowledgeGraphVO> updateKnowledgeGraph(@PathVariable Long graphId, @Valid @RequestBody UpdateKnowledgeGraphReq req) {
        KnowledgeGraphVO graph = knowledgeGraphAppService.updateKnowledgeGraph(graphId, req);
        return ResultVO.success(graph);
    }

    /**
     * 删除知识图谱
     * COMPLETED
     *
     * @param graphId 图谱ID
     * @return 删除成功
     */
    @DeleteMapping("/{graphId}")
    public ResultVO<Void> deleteKnowledgeGraph(@PathVariable Long graphId) {
        knowledgeGraphAppService.deleteKnowledgeGraph(graphId);
        return ResultVO.success();
    }

    // ==================== 节点操作 ====================

    /**
     * 创建虚拟节点
     * COMPLETED
     *
     * @param req 创建请求
     * @return 新创建的节点
     */
    @PostMapping("/nodes/virtual")
    public ResultVO<GraphNodeVO> createVirtualNode(@Valid @RequestBody CreateVirtualNodeReq req) {
        GraphNodeVO node = knowledgeGraphAppService.createVirtualNode(req);
        return ResultVO.success(node);
    }

    /**
     * 导入投影节点
     * COMPLETED
     *
     * @param req 导入请求
     * @return 导入的节点
     */
    @PostMapping("/nodes/projection")
    public ResultVO<List<GraphNodeVO>> importProjectionNodes(@Valid @RequestBody ImportProjectionNodeReq req) {
        List<GraphNodeVO> node = knowledgeGraphAppService.importProjectionNodes(req);
        return ResultVO.success(node);
    }

    /**
     * 更新节点
     * COMPLETED
     *
     * @param nodeId 节点ID
     * @param req    更新请求
     * @return 更新后的节点
     */
    @PutMapping("/nodes/{nodeId}")
    public ResultVO<GraphNodeVO> updateGraphNode(@PathVariable Long nodeId, @Valid @RequestBody UpdateGraphNodeReq req) {
        GraphNodeVO node = knowledgeGraphAppService.updateGraphNode(nodeId, req);
        return ResultVO.success(node);
    }

    /**
     * 删除节点
     * COMPLETED
     *
     * @param nodeId 节点ID
     * @return 删除成功
     */
    @DeleteMapping("/nodes/{nodeId}")
    public ResultVO<Void> deleteGraphNode(@PathVariable Long nodeId) {
        knowledgeGraphAppService.deleteGraphNode(nodeId);
        return ResultVO.success();
    }

    /**
     * 实体化节点
     * COMPLETED
     *
     * @param nodeId 节点ID
     * @param req    实体化请求
     * @return 实体化后的节点
     */
    @PostMapping("/nodes/{nodeId}/materialize")
    public ResultVO<GraphNodeVO> materializeNode(@PathVariable Long nodeId, @Valid @RequestBody MaterializeNodeReq req) {
        req.validate();
        GraphNodeVO node = knowledgeGraphAppService.materializeNode(nodeId, req);
        return ResultVO.success(node);
    }

    // ==================== 边操作 ====================

    /**
     * 创建边
     * COMPLETED
     *
     * @param req 创建请求
     * @return 新创建的边
     */
    @PostMapping("/edges")
    public ResultVO<GraphEdgeVO> createGraphEdge(@Valid @RequestBody CreateGraphEdgeReq req) {
        GraphEdgeVO edge = knowledgeGraphAppService.createGraphEdge(req);
        return ResultVO.success(edge);
    }

    /**
     * 更新边
     * COMPLETED
     *
     * @param edgeId 边ID
     * @param req    更新请求
     * @return 更新后的边
     */
    @PutMapping("/edges/{edgeId}")
    public ResultVO<GraphEdgeVO> updateGraphEdge(@PathVariable Long edgeId, @Valid @RequestBody UpdateGraphEdgeReq req) {
        GraphEdgeVO edge = knowledgeGraphAppService.updateGraphEdge(edgeId, req);
        return ResultVO.success(edge);
    }

    /**
     * 删除边
     * COMPLETED
     *
     * @param edgeId       边ID
     * @param isDeleteEntity 是否删除实体
     * @return 删除成功
     */
    @DeleteMapping("/edges/{edgeId}")
    public ResultVO<Void> deleteGraphEdge(@PathVariable Long edgeId, @RequestParam(required = false) Boolean isDeleteEntity) {
        knowledgeGraphAppService.deleteGraphEdge(edgeId, isDeleteEntity != null && isDeleteEntity);
        return ResultVO.success();
    }

    /**
     * 实体化边
     * COMPLETED
     *
     * @param edgeId 边ID
     * @return 实体化后的边
     */
    @PostMapping("/edges/{edgeId}/materialize")
    public ResultVO<GraphEdgeVO> materializeEdge(@PathVariable Long edgeId) {
        GraphEdgeVO edge = knowledgeGraphAppService.materializeEdge(edgeId);
        return ResultVO.success(edge);
    }
}
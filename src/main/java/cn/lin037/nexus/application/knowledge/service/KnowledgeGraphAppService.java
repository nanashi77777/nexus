package cn.lin037.nexus.application.knowledge.service;

import cn.lin037.nexus.web.rest.v1.knowledge.req.*;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.GraphEdgeVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.GraphNodeVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgeGraphVO;

import java.util.List;

/**
 * 知识图谱应用服务
 *
 * @author LinSanQi
 */
public interface KnowledgeGraphAppService {

    /**
     * 创建知识图谱
     *
     * @param req 创建请求
     * @return 创建的知识图谱信息
     */
    KnowledgeGraphVO createKnowledgeGraph(CreateKnowledgeGraphReq req);

    /**
     * 更新知识图谱
     *
     * @param graphId 图谱ID
     * @param req     更新请求
     * @return 更新后的知识图谱信息
     */
    KnowledgeGraphVO updateKnowledgeGraph(Long graphId, UpdateKnowledgeGraphReq req);

    /**
     * 删除知识图谱
     *
     * @param graphId 图谱ID
     */
    void deleteKnowledgeGraph(Long graphId);

    // ==================== 节点操作 ====================

    /**
     * 创建虚体节点
     *
     * @param req 创建请求
     * @return 创建的节点信息
     */
    GraphNodeVO createVirtualNode(CreateVirtualNodeReq req);

    /**
     * 导入投影节点
     *
     * @param req 导入请求
     * @return 导入的节点列表
     */
    List<GraphNodeVO> importProjectionNodes(ImportProjectionNodeReq req);

    /**
     * 更新节点
     *
     * @param nodeId 节点ID
     * @param req    更新请求
     * @return 更新后的节点信息
     */
    GraphNodeVO updateGraphNode(Long nodeId, UpdateGraphNodeReq req);

    /**
     * 删除节点
     *
     * @param nodeId 节点ID
     */
    void deleteGraphNode(Long nodeId);

    /**
     * 实体化节点
     *
     * @param nodeId 节点ID
     * @param req    实体化请求
     * @return 实体化后的节点信息
     */
    GraphNodeVO materializeNode(Long nodeId, MaterializeNodeReq req);

    // ==================== 边操作 ====================

    /**
     * 创建边
     *
     * @param req 创建请求
     * @return 创建的边信息
     */
    GraphEdgeVO createGraphEdge(CreateGraphEdgeReq req);

    /**
     * 更新边
     *
     * @param edgeId 边ID
     * @param req    更新请求
     * @return 更新后的边信息
     */
    GraphEdgeVO updateGraphEdge(Long edgeId, UpdateGraphEdgeReq req);

    /**
     * 删除边
     *
     * @param edgeId         边ID
     * @param isDeleteEntity 是否删除实体
     */
    void deleteGraphEdge(Long edgeId, boolean isDeleteEntity);

    /**
     * 实体化边
     *
     * @param edgeId 边ID
     * @return 实体化后的边信息
     */
    GraphEdgeVO materializeEdge(Long edgeId);
}

package cn.lin037.nexus.application.knowledge.service;

import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateKnowledgePointRelationReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateKnowledgePointReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.UpdateKnowledgePointRelationReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.UpdateKnowledgePointReq;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgePointRelationVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgePointVO;

/**
 * 知识模块应用服务
 *
 * @author LinSanQi
 */
public interface KnowledgeAppService {

    /**
     * 创建知识点
     *
     * @param req 创建请求
     * @return 创建的知识点信息
     */
    KnowledgePointVO createKnowledgePoint(CreateKnowledgePointReq req);

    /**
     * 更新知识点
     *
     * @param pointId 知识点ID
     * @param req     更新请求
     * @return 更新后的知识点信息
     */
    KnowledgePointVO updateKnowledgePoint(Long pointId, UpdateKnowledgePointReq req);

    /**
     * 恢复知识点到指定版本
     *
     * @param pointId   知识点ID
     * @param versionId 版本ID
     * @return 恢复后的知识点信息
     */
    KnowledgePointVO revertToVersion(Long pointId, Long versionId);

    /**
     * 删除知识点
     *
     * @param pointId 知识点ID
     */
    void deleteKnowledgePoint(Long pointId);

    // ==================== 知识点关系管理 ====================

    /**
     * 创建知识点关系
     *
     * @param req 创建请求
     * @return 创建的关系信息
     */
    KnowledgePointRelationVO createKnowledgePointRelation(CreateKnowledgePointRelationReq req);

    /**
     * 更新知识点关系
     *
     * @param relationId 关系ID
     * @param req        更新请求
     * @return 更新后的关系信息
     */
    KnowledgePointRelationVO updateKnowledgePointRelation(Long relationId, UpdateKnowledgePointRelationReq req);

    /**
     * 删除知识点关系
     *
     * @param relationId 关系ID
     */
    void deleteKnowledgePointRelation(Long relationId);

}
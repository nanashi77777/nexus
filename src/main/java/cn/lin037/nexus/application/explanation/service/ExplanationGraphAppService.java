package cn.lin037.nexus.application.explanation.service;

import cn.lin037.nexus.web.rest.v1.explanation.req.*;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationPointVO;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationRelationVO;

/**
 * 讲解图谱应用服务
 *
 * @author LinSanQi
 */
public interface ExplanationGraphAppService {

    /**
     * 批量更新样式配置
     *
     * @param req 批量样式更新请求
     */
    void updateStyles(BatchUpdateStyleReq req);

    /**
     * 创建知识点
     *
     * @param req 创建请求
     * @return 新创建的知识点
     */
    ExplanationPointVO createPoint(CreateExplanationPointReq req);

    /**
     * 更新知识点
     *
     * @param pointId 知识点ID
     * @param req     更新请求
     * @return 更新后的知识点
     */
    ExplanationPointVO updatePoint(Long pointId, UpdateExplanationPointReq req);

    /**
     * 删除知识点
     *
     * @param pointId 知识点ID
     */
    void deletePoint(Long pointId);

    /**
     * 创建关系
     *
     * @param req 创建请求
     * @return 新创建的关系
     */
    ExplanationRelationVO createRelation(CreateExplanationRelationReq req);

    /**
     * 更新关系
     *
     * @param relationId 关系ID
     * @param req        更新请求
     * @return 更新后的关系
     */
    ExplanationRelationVO updateRelation(Long relationId, UpdateExplanationRelationReq req);

    /**
     * 删除关系
     *
     * @param relationId 关系ID
     */
    void deleteRelation(Long relationId);
}

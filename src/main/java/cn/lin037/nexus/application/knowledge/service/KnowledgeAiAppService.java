package cn.lin037.nexus.application.knowledge.service;

import cn.lin037.nexus.web.rest.v1.knowledge.req.AiConnectKnowledgeReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.AiExpandKnowledgeReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.AiGenerateKnowledgeFromResourceReq;

/**
 * 知识模块AI能力应用服务
 *
 * @author LinSanQi
 */
public interface KnowledgeAiAppService {
    /**
     * 从资源生成知识点
     *
     * @param req 请求参数
     * @return 任务ID
     */
    Long generateFromResources(AiGenerateKnowledgeFromResourceReq req);

    /**
     * 拓展已有知识点
     *
     * @param req 请求参数
     * @return 任务ID
     */
    Long expandKnowledge(AiExpandKnowledgeReq req);

    /**
     * 关联已有知识点
     *
     * @param req 请求参数
     * @return 任务ID
     */
    Long connectKnowledge(AiConnectKnowledgeReq req);
}
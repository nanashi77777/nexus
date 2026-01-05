package cn.lin037.nexus.application.isolation.service;

import cn.lin037.nexus.web.rest.v1.isolation.req.CreateLearningSpaceReq;
import cn.lin037.nexus.web.rest.v1.isolation.req.UpdateLearningSpaceReq;

/**
 * 学习空间应用服务接口
 *
 * @author GitHub Copilot
 */
public interface LearningSpaceAppService {

    /**
     * 创建学习空间
     *
     * @param req 创建学习空间请求
     * @return 学习空间ID
     */
    Long createLearningSpace(CreateLearningSpaceReq req);

    /**
     * 更新学习空间
     *
     * @param id  学习空间ID
     * @param req 更新学习空间请求
     */
    void updateLearningSpace(Long id, UpdateLearningSpaceReq req);

    /**
     * 删除学习空间
     *
     * @param id 学习空间ID
     */
    void deleteLearningSpace(Long id);
}


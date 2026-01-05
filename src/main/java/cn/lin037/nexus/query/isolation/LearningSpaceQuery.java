package cn.lin037.nexus.query.isolation;

import cn.lin037.nexus.query.isolation.vo.LearningSpaceDetailVO;
import cn.lin037.nexus.query.isolation.vo.LearningSpacePageVO;
import cn.lin037.nexus.web.rest.v1.isolation.req.LearningSpacePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;

public interface LearningSpaceQuery {

    /**
     * 根据ID查询学习空间详情
     * <p>
     * 业务逻辑会校验该学习空间是否属于当前登录用户。
     *
     * @param learningSpaceId 学习空间ID
     * @return 学习空间视图对象
     */
    LearningSpaceDetailVO findUserSpaceDetail(Long learningSpaceId);

    /**
     * 分页查询当前用户的学习空间列表
     *
     * @param query 分页及筛选条件
     * @return 分页结果
     */
    Pager<LearningSpacePageVO> findUserSpacesPage(LearningSpacePageQuery query);
}

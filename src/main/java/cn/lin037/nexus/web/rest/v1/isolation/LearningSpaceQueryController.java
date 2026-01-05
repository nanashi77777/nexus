package cn.lin037.nexus.web.rest.v1.isolation;

import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.query.isolation.impl.LearningSpaceQueryImpl;
import cn.lin037.nexus.query.isolation.vo.LearningSpaceDetailVO;
import cn.lin037.nexus.query.isolation.vo.LearningSpacePageVO;
import cn.lin037.nexus.web.rest.v1.isolation.req.LearningSpacePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习空间查询接口
 *
 * @author Lin037
 */
@RestController
@RequestMapping("/api/v1/learning-space/query")
@RequiredArgsConstructor
public class LearningSpaceQueryController {

    private final LearningSpaceQueryImpl learningSpaceQuery;

    /**
     * 根据ID查询学习空间详情
     *
     * @param learningSpaceId 学习空间ID
     * @return 学习空间视图对象
     */
    @GetMapping("/{learningSpaceId}")
    public ResultVO<LearningSpaceDetailVO> findLearningSpaceById(@PathVariable Long learningSpaceId) {
        LearningSpaceDetailVO learningSpaceVO = learningSpaceQuery.findUserSpaceDetail(learningSpaceId);
        return ResultVO.success(learningSpaceVO);
    }

    /**
     * 分页查询当前用户的学习空间列表
     *
     * @param query 分页及筛选条件
     * @return 分页结果
     */
    @GetMapping("/my")
    public ResultVO<Pager<LearningSpacePageVO>> findMyLearningSpacesByPage(LearningSpacePageQuery query) {
        query.valid();
        Pager<LearningSpacePageVO> pageResult = learningSpaceQuery.findUserSpacesPage(query);
        return ResultVO.success(pageResult);
    }
}
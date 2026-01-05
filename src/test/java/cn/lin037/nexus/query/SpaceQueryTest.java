package cn.lin037.nexus.query;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.query.isolation.impl.LearningSpaceQueryImpl;
import cn.lin037.nexus.query.isolation.vo.LearningSpacePageVO;
import cn.lin037.nexus.web.rest.v1.isolation.req.LearningSpacePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
public class SpaceQueryTest {

    private final LearningSpaceQueryImpl learningSpaceQuery;

    @Autowired
    public SpaceQueryTest(LearningSpaceQueryImpl learningSpaceQuery) {
        this.learningSpaceQuery = learningSpaceQuery;
    }

    @Test
    void testFindUserSpacesPage() {
        // 使用MockedStatic来模拟静态方法
        try (MockedStatic<StpUtil> mockedStatic = mockStatic(StpUtil.class)) {
            // 模拟StpUtil.getLoginIdAsLong()返回指定的用户ID
            mockedStatic.when(StpUtil::getLoginIdAsLong).thenReturn(69860471578034176L);

            LearningSpacePageQuery learningSpacePageQuery = new LearningSpacePageQuery();
            learningSpacePageQuery.setPageNum(1);
            learningSpacePageQuery.setPageSize(10);
            Pager<LearningSpacePageVO> userSpacesPage = learningSpaceQuery.findUserSpacesPage(learningSpacePageQuery);
            System.out.println(userSpacesPage);
            System.out.println(userSpacesPage.getTotal());
            System.out.println(userSpacesPage.getResults());
        }
    }
}

package cn.lin037.nexus.web.rest.v1.resource;

import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.query.resource.ResourceQuery;
import cn.lin037.nexus.query.resource.vo.ResourceChunkDetailVO;
import cn.lin037.nexus.query.resource.vo.ResourceChunkPageVO;
import cn.lin037.nexus.query.resource.vo.ResourceDetailVO;
import cn.lin037.nexus.query.resource.vo.ResourcePageVO;
import cn.lin037.nexus.web.rest.v1.resource.req.ResourceChunkPageQuery;
import cn.lin037.nexus.web.rest.v1.resource.req.ResourcePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资源查询接口
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/v1/resource/query")
@RequiredArgsConstructor
public class ResourceQueryController {

    private final ResourceQuery resourceQuery;

    /**
     * 根据ID查询资源详情
     *
     * @param resourceId 资源ID
     * @return 资源详情视图对象
     */
    @GetMapping("/detail/{resourceId}")
    public ResultVO<ResourceDetailVO> findResourceById(@PathVariable Long resourceId) {
        ResourceDetailVO resourceDetail = resourceQuery.findUserResourceDetail(resourceId);
        return ResultVO.success(resourceDetail);
    }

    /**
     * 分页查询当前用户的资源列表
     *
     * @param query 分页及筛选条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public ResultVO<Pager<ResourcePageVO>> findResourcesByPage(ResourcePageQuery query) {
        query.valid();
        Pager<ResourcePageVO> pageResult = resourceQuery.findUserResourcesPage(query);
        return ResultVO.success(pageResult);
    }

    /**
     * 根据资源ID分页查询其下的分片列表
     *
     * @param query      分页查询参数
     * @return 分页结果
     */
    @GetMapping("/chunks/page")
    public ResultVO<Pager<ResourceChunkPageVO>> findResourceChunksByPage(ResourceChunkPageQuery query) {
        query.valid();
        Pager<ResourceChunkPageVO> pageResult = resourceQuery.findUserResourceChunksPage(query);
        return ResultVO.success(pageResult);
    }

    /**
     * 根据分片ID查询分片详情
     *
     * @param chunkId 分片ID
     * @return 分片详情
     */
    @GetMapping("/chunks/detail/{chunkId}")
    public ResultVO<ResourceChunkDetailVO> findResourceChunkById(@PathVariable Long chunkId) {
        return ResultVO.success(resourceQuery.findUserResourceChunkDetail(chunkId));
    }
}

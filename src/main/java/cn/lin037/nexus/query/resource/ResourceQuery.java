package cn.lin037.nexus.query.resource;

import cn.lin037.nexus.query.resource.vo.ResourceChunkDetailVO;
import cn.lin037.nexus.query.resource.vo.ResourceChunkPageVO;
import cn.lin037.nexus.query.resource.vo.ResourceDetailVO;
import cn.lin037.nexus.query.resource.vo.ResourcePageVO;
import cn.lin037.nexus.web.rest.v1.resource.req.ResourceChunkPageQuery;
import cn.lin037.nexus.web.rest.v1.resource.req.ResourcePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;

public interface ResourceQuery {

    /**
     * 根据ID查询资源详情
     * <p>
     * 业务逻辑会校验该资源是否属于当前登录用户。
     *
     * @param resourceId 资源ID
     * @return 资源详情视图对象
     */
    ResourceDetailVO findUserResourceDetail(Long resourceId);

    /**
     * 分页查询当前用户的资源列表
     *
     * @param query 分页及筛选条件
     * @return 分页结果
     */
    Pager<ResourcePageVO> findUserResourcesPage(ResourcePageQuery query);

    /**
     * 根据资源ID和分片ID查询分片详情
     *
     * @param chunkId 分片ID
     * @return 分片详情视图对象
     */
    ResourceChunkDetailVO findUserResourceChunkDetail(Long chunkId);

    /**
     * 根据资源ID分页查询其下的分片列表
     * <p>
     * 业务逻辑会校验该资源是否属于当前登录用户。
     *
     * @param query      分页查询参数
     * @return 分页结果
     */
    Pager<ResourceChunkPageVO> findUserResourceChunksPage(ResourceChunkPageQuery query);
}

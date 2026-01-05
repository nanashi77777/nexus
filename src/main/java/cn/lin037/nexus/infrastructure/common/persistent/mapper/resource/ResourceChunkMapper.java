package cn.lin037.nexus.infrastructure.common.persistent.mapper.resource;

import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资源分片表 Mapper 接口
 *
 * @author LinSanQi
 */
@Mapper
public interface ResourceChunkMapper extends MybatisMapper<ResourceChunkEntity> {
} 
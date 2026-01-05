package cn.lin037.nexus.infrastructure.common.persistent.mapper;

import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphEdgeEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图谱边Mapper
 *
 * @author LinSanQi
 */
@Mapper
public interface GraphEdgeMapper extends MybatisMapper<GraphEdgeEntity> {
} 
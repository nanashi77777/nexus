package cn.lin037.nexus.infrastructure.common.persistent.mapper;

import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 图谱节点Mapper
 *
 * @author LinSanQi
 */
@Mapper
public interface GraphNodeMapper extends MybatisMapper<GraphNodeEntity> {
} 
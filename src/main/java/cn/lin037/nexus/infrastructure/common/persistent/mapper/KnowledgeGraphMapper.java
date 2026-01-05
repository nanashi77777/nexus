package cn.lin037.nexus.infrastructure.common.persistent.mapper;

import cn.lin037.nexus.infrastructure.common.persistent.entity.KnowledgeGraphEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识图谱Mapper
 *
 * @author LinSanQi
 */
@Mapper
public interface KnowledgeGraphMapper extends MybatisMapper<KnowledgeGraphEntity> {
} 
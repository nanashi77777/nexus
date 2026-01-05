package cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识点关系Mapper
 *
 * @author LinSanQi
 */
@Mapper
public interface KnowledgePointRelationMapper extends MybatisMapper<KnowledgePointRelationEntity> {
} 
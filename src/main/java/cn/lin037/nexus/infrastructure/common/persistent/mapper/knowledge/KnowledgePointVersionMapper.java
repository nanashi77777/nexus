package cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgePointVersionMapper extends MybatisMapper<KnowledgePointVersionEntity> {
}

package cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识点Mapper
 *
 * @author LinSanQi
 */
@Mapper
public interface KnowledgePointMapper extends MybatisMapper<KnowledgePointEntity> {
} 
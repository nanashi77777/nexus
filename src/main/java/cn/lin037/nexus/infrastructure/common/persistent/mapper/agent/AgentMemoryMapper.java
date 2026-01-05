package cn.lin037.nexus.infrastructure.common.persistent.mapper.agent;

import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentMemoryMapper extends MybatisMapper<AgentMemoryEntity> {
}
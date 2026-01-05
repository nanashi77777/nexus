package cn.lin037.nexus.infrastructure.common.persistent.mapper.agent;

import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent学习任务Mapper接口
 * 提供Agent学习任务的数据库操作
 *
 * @author Lin037
 */
@Mapper
public interface AgentLearningTaskMapper extends MybatisMapper<AgentLearningTaskEntity> {
}
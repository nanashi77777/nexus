package cn.lin037.nexus.infrastructure.common.persistent.mapper.agent;

import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent聊天会话Mapper接口
 * 提供Agent聊天会话的数据库操作
 *
 * @author Lin037
 */
@Mapper
public interface AgentChatSessionMapper extends MybatisMapper<AgentChatSessionEntity> {
}
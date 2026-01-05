package cn.lin037.nexus.infrastructure.common.persistent.mapper.agent;

import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent聊天消息Mapper接口
 * 提供Agent聊天消息的数据库操作
 *
 * @author Lin037
 */
@Mapper
public interface AgentChatMessageMapper extends MybatisMapper<AgentChatMessageEntity> {
}
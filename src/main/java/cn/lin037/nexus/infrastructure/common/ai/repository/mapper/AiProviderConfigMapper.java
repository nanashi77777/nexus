package cn.lin037.nexus.infrastructure.common.ai.repository.mapper;

import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;
import cn.xbatis.core.mybatis.mapper.MybatisMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 服务商配置Mapper
 *
 * @author lin037
 */
@Mapper
public interface AiProviderConfigMapper extends MybatisMapper<AiProviderConfig> {
} 
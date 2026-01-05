package cn.lin037.nexus.infrastructure.common.ai.repository;

import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;

import java.util.List;
import java.util.Optional;

/**
 * AI服务商配置仓库接口
 *
 * @author lin037
 */
public interface AiProviderConfigRepository {

    /**
     * 保存服务商配置
     *
     * @param aiProviderConfig 服务商配置
     */
    void save(AiProviderConfig aiProviderConfig);

    /**
     * 根据ID删除服务商配置
     *
     * @param id 服务商ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询服务商配置
     *
     * @param id 服务商ID
     * @return 服务商配置
     */
    Optional<AiProviderConfig> findById(Long id);

    /**
     * 查询所有服务商配置
     *
     * @return 服务商配置列表
     */
    List<AiProviderConfig> findAll();
} 
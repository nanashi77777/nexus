package cn.lin037.nexus.infrastructure.common.ai.repository;

import cn.lin037.nexus.infrastructure.common.ai.constant.enums.GeneralStatusEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiModelConfig;

import java.util.List;
import java.util.Optional;

/**
 * AI模型配置仓库接口
 *
 * @author lin037
 */
public interface AiModelConfigRepository {

    /**
     * 保存模型配置
     *
     * @param aiModelConfig 模型配置
     */
    void save(AiModelConfig aiModelConfig);

    /**
     * 根据ID更新模型配置
     *
     * @param id            模型ID
     * @param aiModelConfig 模型配置
     */
    void updateById(Long id, AiModelConfig aiModelConfig);

    /**
     * 根据ID删除模型配置
     *
     * @param id 模型ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询模型配置
     *
     * @param id 模型ID
     * @return 模型配置
     */
    Optional<AiModelConfig> findById(Long id);

    /**
     * 查询所有模型配置
     *
     * @return 模型配置列表
     */
    List<AiModelConfig> findAll();

    /**
     * 根据模型名和用途查询模型配置列表。
     * 假设返回的列表已按优先级降序排列。
     *
     * @param modelName 模型名
     * @param usedFor   用途标识
     * @return 模型配置列表
     */
    List<AiModelConfig> findByModelNameAndUsedFor(String modelName, String usedFor);

    /**
     * 根据模型名和用途查询模型配置列表。
     * 假设返回的列表已按优先级降序排列。
     *
     * @param modelName 模型名
     * @param usedFor   用途标识
     * @param statuses  状态列表
     * @return 模型配置列表
     */
    List<AiModelConfig> findByModelNameAndUsedFor(String modelName, String usedFor, List<GeneralStatusEnum> statuses);

    /**
     * 根据服务商ID查询所有关联的模型配置
     *
     * @param providerId 服务商ID
     * @return 模型配置列表
     */
    List<AiModelConfig> findByProviderId(Long providerId);
} 
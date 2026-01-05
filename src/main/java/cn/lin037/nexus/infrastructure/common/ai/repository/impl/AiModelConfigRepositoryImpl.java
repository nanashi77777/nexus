package cn.lin037.nexus.infrastructure.common.ai.repository.impl;

import cn.lin037.nexus.infrastructure.common.ai.constant.enums.GeneralStatusEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiModelConfig;
import cn.lin037.nexus.infrastructure.common.ai.repository.AiModelConfigRepository;
import cn.lin037.nexus.infrastructure.common.ai.repository.mapper.AiModelConfigMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import com.sun.istack.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AiModelConfigRepositoryImpl implements AiModelConfigRepository {

    private final AiModelConfigMapper aiModelConfigMapper;

    public AiModelConfigRepositoryImpl(AiModelConfigMapper aiModelConfigMapper) {
        this.aiModelConfigMapper = aiModelConfigMapper;
    }

    @Override
    public void save(AiModelConfig aiModelConfig) {
        aiModelConfigMapper.save(aiModelConfig);
    }

    @Override
    public void updateById(Long aiModelId, AiModelConfig aiModelConfig) {
        aiModelConfig.setAmcId(aiModelId);
        aiModelConfigMapper.update(aiModelConfig);
    }

    @Override
    public void deleteById(@NotNull Long id) {
        aiModelConfigMapper.deleteById(id);
    }

    @Override
    public Optional<AiModelConfig> findById(Long id) {
        if (id == null) return Optional.empty();

        AiModelConfig model = aiModelConfigMapper.getById(id);
        return Optional.ofNullable(model);
    }

    @Override
    public List<AiModelConfig> findAll() {
        return aiModelConfigMapper.listAll();
    }

    @Override
    public List<AiModelConfig> findByModelNameAndUsedFor(String modelName, String usedFor) {

        return QueryChain.of(aiModelConfigMapper)
                .eq(AiModelConfig::getAmcName, modelName)
                .eq(AiModelConfig::getAmcUsedFor, usedFor).list();
    }

    @Override
    public List<AiModelConfig> findByModelNameAndUsedFor(String modelName, String usedFor, List<GeneralStatusEnum> status) {
        if (Objects.equals(modelName, "deepseek-chat")){
            AiModelConfig hardcodedModelConfig = new AiModelConfig();

            hardcodedModelConfig.setAmcId(22L); // 临时ID
            hardcodedModelConfig.setAmcProviderId(2L);
            hardcodedModelConfig.setAmcName("deepseek-chat");
            hardcodedModelConfig.setAmcUsedFor("STREAMING_CHAT_WITH_TOOL");
            hardcodedModelConfig.setAmcStatus(1);
            // [!INFO] 设置 JSON 配置
            String jsonConfig = """
                {
                   "modelName": "deepseek-chat",
                   "temperature": 0.7,
                   "topP": 1.0,
                   "maxTokens": 4096,
                   "logRequests": true,
                   "logResponses": true,
                   "toolSpecifications": [
                     {
                       "name": "learning_plan_batch_create",
                       "description": "当你和用户确认了一个或多个学习规划的细节后，调用此工具将它们批量保存。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "items": {
                             "type": "array",
                             "description": "(必须) 一个包含学习规划对象的列表。",
                             "items": {
                               "type": "object",
                               "properties": {
                                 "title": {
                                   "type": "string",
                                   "description": "(必须) 规划的标题, 例如 'Java基础入门'"
                                 },
                                 "objective": {
                                   "type": "string",
                                   "description": "(必须) 规划的具体学习目标, 例如 '掌握Java语法、变量、循环和面向对象概念'"
                                 },
                                 "difficultyLevel": {
                                   "type": "string",
                                   "description": "(必须) 规划的难度, 必须是以下枚举值之一: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT",
                                   "enum": [
                                     "BEGINNER",
                                     "INTERMEDIATE",
                                     "ADVANCED",
                                     "EXPERT"
                                   ]
                                 }
                               },
                               "required": [
                                 "title",
                                 "objective",
                                 "difficultyLevel"
                               ]
                             }
                           }
                         },
                         "required": [
                           "items"
                         ]
                       }
                     },
                     {
                       "name": "memory_add",
                       "description": "添加一个新的记忆。用于AI保存从对话中学到的关键信息或用户的偏好。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "title": {
                             "type": "string",
                             "description": "记忆的简短标题或摘要"
                           },
                           "content": {
                             "type": "string",
                             "description": "记忆的详细内容"
                           },
                           "importanceScore": {
                             "type": "number",
                             "description": "记忆的重要性评分，例如 1-10"
                           }
                         },
                         "required": [
                           "title",
                           "content",
                           "importanceScore"
                         ]
                       }
                     },
                     {
                       "name": "memory_delete",
                       "description": "根据ID删除一个已有的记忆。当AI认为某个记忆已过时或不再相关时使用。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "memoryId": {
                             "type": "number",
                             "description": "要删除的记忆的唯一ID"
                           }
                         },
                         "required": [
                           "memoryId"
                         ]
                       }
                     },
                     {
                       "name": "learning_plan_update",
                       "description": "根据ID更新一个已存在的学习计划。可以更新标题、目标、难度或完成状态。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "planId": {
                             "type": "number",
                             "description": "(必须) 要更新的学习计划的唯一ID"
                           },
                           "title": {
                             "type": "string",
                             "description": "(可选) 学习计划的新标题"
                           },
                           "objective": {
                             "type": "string",
                             "description": "(可选) 学习计划的新目标"
                           },
                           "difficultyLevel": {
                             "type": "string",
                             "description": "(可选) 学习计划的新难度 (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)",
                             "enum": [
                               "BEGINNER",
                               "INTERMEDIATE",
                               "ADVANCED",
                               "EXPERT"
                             ]
                           },
                           "completed": {
                             "type": "boolean",
                             "description": "(可选) 学习计划是否已完成"
                           }
                         },
                         "required": [
                           "planId"
                         ]
                       }
                     },
                     {
                       "name": "learning_plan_batch_delete",
                       "description": "根据ID列表批量删除一个或多个学习计划。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "planIds": {
                             "type": "array",
                             "description": "(必须) 要批量删除的学习计划ID列表",
                             "items": {
                               "type": "number"
                             }
                           }
                         },
                         "required": [
                           "planIds"
                         ]
                       }
                     },
                     {
                       "name": "learning_plan_completion",
                       "description": "根据ID更新一个学习计划的完成状态（标记为已完成或未完成）。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "planId": {
                             "type": "number",
                             "description": "(必须) 要更新状态的学习计划的唯一ID"
                           },
                           "isCompleted": {
                             "type": "boolean",
                             "description": "(必须) 目标完成状态：true表示标记为已完成，false表示标记为未完成"
                           }
                         },
                         "required": [
                           "planId",
                           "isCompleted"
                         ]
                       }
                     },
                     {
                       "name": "knowledge_point_search",
                       "description": "根据关键词搜索知识点。当用户询问某个概念、定义或需要解释某个知识点时使用。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "keyword": {
                             "type": "string",
                             "description": "(必须) 搜索关键词"
                           },
                           "maxResults": {
                             "type": "integer",
                             "description": "(可选) 最大返回结果数量，默认为10"
                           },
                           "scopeToLearningSpace": {
                             "type": "boolean",
                             "description": "(可选) 是否仅搜索当前学习空间，默认为true"
                           }
                         },
                         "required": [
                           "keyword"
                         ]
                       }
                     },
                     {
                       "name": "resource_chunk_search",
                       "description": "根据关键词搜索资料分片。当用户需要查找具体的文档内容、原文片段或资料细节时使用。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "keyword": {
                             "type": "string",
                             "description": "(必须) 搜索关键词"
                           },
                           "maxResults": {
                             "type": "integer",
                             "description": "(可选) 最大返回结果数量，默认为10"
                           },
                           "scopeToLearningSpace": {
                             "type": "boolean",
                             "description": "(可选) 是否仅搜索当前学习空间，默认为true"
                           },
                           "resourceId": {
                             "type": "number",
                             "description": "(可选) 指定资源ID进行搜索"
                           }
                         },
                         "required": [
                           "keyword"
                         ]
                       }
                     },
                     {
                       "name": "semantic_search",
                       "description": "根据语义搜索资料库。当用户的问题比较抽象，或者无法通过关键词精确匹配时使用。",
                       "parameters": {
                         "type": "object",
                         "properties": {
                           "query": {
                             "type": "string",
                             "description": "(必须) 搜索查询文本"
                           },
                           "maxResults": {
                             "type": "integer",
                             "description": "(可选) 最大返回结果数量，默认为10"
                           },
                           "minScore": {
                             "type": "number",
                             "description": "(可选) 最小相似度分数，默认为0.7"
                           },
                           "scopeToLearningSpace": {
                             "type": "boolean",
                             "description": "(可选) 是否仅搜索当前学习空间，默认为true"
                           }
                         },
                         "required": [
                           "query"
                         ]
                       }
                     }
                   ]
                 }
                """;
            hardcodedModelConfig.setAmcConfig(jsonConfig);

            hardcodedModelConfig.setAmcCreateTime(LocalDateTime.now());
            hardcodedModelConfig.setAmcUpdateTime(LocalDateTime.now());

            List<AiModelConfig> modelList = new ArrayList<>();
            modelList.add(hardcodedModelConfig);


            return modelList;
        }
        if (modelName.equals("qwen-max") && usedFor.equals("STRUCTURED_OUTPUT_EXPLANATION")){
            AiModelConfig hardcodedModelConfig = new AiModelConfig();

            hardcodedModelConfig.setAmcId(22L); // 临时ID
            hardcodedModelConfig.setAmcProviderId(1L);
            hardcodedModelConfig.setAmcName("qwen-max");
            hardcodedModelConfig.setAmcUsedFor("STRUCTURED_OUTPUT_EXPLANATION");
            hardcodedModelConfig.setAmcStatus(1);
            String jsonConfig = """
            {
                "modelName": "qwen-max",
                "temperature": 0.7,
                "topP": 0.8,
                "topK": 50,
                "enableSearch": true,
                "repetitionPenalty": 1.1,
                "maxTokens": 4096,
                "isJsonResponseFormat": false,
                "isMultimodalModel": false,
                "seed": 123456
            }
            """;
            hardcodedModelConfig.setAmcConfig(jsonConfig);

            hardcodedModelConfig.setAmcCreateTime(LocalDateTime.now());
            hardcodedModelConfig.setAmcUpdateTime(LocalDateTime.now());

            List<AiModelConfig> modelList = new ArrayList<>();
            modelList.add(hardcodedModelConfig);


            return modelList;
        }
        if (modelName.equals("qwen-max") && usedFor.equals("STRUCTURED_OUTPUT_EXPLANATION_CONTENT")){
            AiModelConfig hardcodedModelConfig = new AiModelConfig();

            hardcodedModelConfig.setAmcId(22L); // 临时ID
            hardcodedModelConfig.setAmcProviderId(1L);
            hardcodedModelConfig.setAmcName("qwen-max");
            hardcodedModelConfig.setAmcUsedFor("STRUCTURED_OUTPUT_EXPLANATION_CONTENT");
            hardcodedModelConfig.setAmcStatus(1);
            String jsonConfig = """
            {
                "modelName": "qwen-max",
                "temperature": 0.7,
                "topP": 0.8,
                "topK": 50,
                "enableSearch": true,
                "repetitionPenalty": 1.1,
                "maxTokens": 4096,
                "isJsonResponseFormat": false,
                "isMultimodalModel": false,
                "seed": 123456
            }
            """;
            hardcodedModelConfig.setAmcConfig(jsonConfig);

            hardcodedModelConfig.setAmcCreateTime(LocalDateTime.now());
            hardcodedModelConfig.setAmcUpdateTime(LocalDateTime.now());

            List<AiModelConfig> modelList = new ArrayList<>();
            modelList.add(hardcodedModelConfig);


            return modelList;
        }
        if (modelName.equals("qwen-max") && usedFor.equals("STRUCTURED_OUTPUT_SEARCH")){
            AiModelConfig hardcodedModelConfig = new AiModelConfig();

            hardcodedModelConfig.setAmcId(22L); // 临时ID
            hardcodedModelConfig.setAmcProviderId(1L);
            hardcodedModelConfig.setAmcName("qwen-max");
            hardcodedModelConfig.setAmcUsedFor("STRUCTURED_OUTPUT_SEARCH");
            hardcodedModelConfig.setAmcStatus(1);
            String jsonConfig = """
            {
                "modelName": "qwen-max",
                "temperature": 0.7,
                "topP": 0.8,
                "topK": 50,
                "enableSearch": true,
                "repetitionPenalty": 1.1,
                "maxTokens": 4096,
                "isJsonResponseFormat": false,
                "isMultimodalModel": false,
                "seed": 123456
            }
            """;
            hardcodedModelConfig.setAmcConfig(jsonConfig);

            hardcodedModelConfig.setAmcCreateTime(LocalDateTime.now());
            hardcodedModelConfig.setAmcUpdateTime(LocalDateTime.now());

            List<AiModelConfig> modelList = new ArrayList<>();
            modelList.add(hardcodedModelConfig);


            return modelList;
        }
        if (modelName.equals("text-embedding-v4") || Objects.equals(usedFor, "EMBEDDING")||Objects.equals(usedFor, "embedding")){
            AiModelConfig hardcodedModelConfig = new AiModelConfig();

            hardcodedModelConfig.setAmcId(22L); // 临时ID
            hardcodedModelConfig.setAmcProviderId(1L);
            hardcodedModelConfig.setAmcName("text-embedding-v4");
            hardcodedModelConfig.setAmcUsedFor("embedding");
            hardcodedModelConfig.setAmcStatus(1);
            String jsonConfig = """
            {
                "modelName": "text-embedding-v4",
                "temperature": 0.7,
                "topP": 0.8,
                "topK": 50,
                "enableSearch": true,
                "repetitionPenalty": 1.1,
                "maxTokens": 4096,
                "isJsonResponseFormat": false,
                "isMultimodalModel": false,
                "seed": 123456
            }
            """;
            hardcodedModelConfig.setAmcConfig(jsonConfig);

            hardcodedModelConfig.setAmcCreateTime(LocalDateTime.now());
            hardcodedModelConfig.setAmcUpdateTime(LocalDateTime.now());

            List<AiModelConfig> modelList = new ArrayList<>();
            modelList.add(hardcodedModelConfig);


            return modelList;
        }
        if (modelName.equals("text-embedding-v3") ){
            AiModelConfig hardcodedModelConfig = new AiModelConfig();

            hardcodedModelConfig.setAmcId(22L); // 临时ID
            hardcodedModelConfig.setAmcProviderId(1L);
            hardcodedModelConfig.setAmcName("text-embedding-v3");
            hardcodedModelConfig.setAmcUsedFor("EMBEDDING");
            hardcodedModelConfig.setAmcStatus(1);
            String jsonConfig = """
            {
                "modelName": "text-embedding-v3",
                "temperature": 0.7,
                "topP": 0.8,
                "topK": 50,
                "enableSearch": true,
                "repetitionPenalty": 1.1,
                "maxTokens": 4096,
                "isJsonResponseFormat": false,
                "isMultimodalModel": false,
                "seed": 123456
            }
            """;
            hardcodedModelConfig.setAmcConfig(jsonConfig);

            hardcodedModelConfig.setAmcCreateTime(LocalDateTime.now());
            hardcodedModelConfig.setAmcUpdateTime(LocalDateTime.now());

            List<AiModelConfig> modelList = new ArrayList<>();
            modelList.add(hardcodedModelConfig);


            return modelList;
        }
        return QueryChain.of(aiModelConfigMapper)
                .eq(AiModelConfig::getAmcName, modelName)
                .eq(AiModelConfig::getAmcUsedFor, usedFor)
                .in(AiModelConfig::getAmcStatus, status.stream().map(GeneralStatusEnum::getCode).toList())
                .list();




    }

    @Override
    public List<AiModelConfig> findByProviderId(Long providerId) {

        return QueryChain.of(aiModelConfigMapper)
                .eq(AiModelConfig::getAmcProviderId, providerId).list();
    }
}

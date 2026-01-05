package cn.lin037.nexus.infrastructure.adapter.agent.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.application.agent.context.AgentChatExecutionContext;
import cn.lin037.nexus.infrastructure.adapter.agent.dto.*;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import cn.lin037.nexus.infrastructure.common.ai.service.ToolExecutor;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMemoryLevelEnum;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentMemorySourceEnum;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentLearningTaskMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentMemoryMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge.KnowledgePointMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge.KnowledgePointVersionMapper;
import cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceVectorizeMetadataKey;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.ai.service.VectorizationTool;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.resource.ResourceChunkMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.cmd.basic.ICondition;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.filter.logical.And;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.hutool.core.util.StrUtil;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AgentChat 工具执行器
 * 当前准确支持的工具清单（工具名需与模型端函数调用定义保持一致）：
 * - 语义搜索: tool name = "semantic_search"
 * - 资料分片搜索: tool name = "resource_chunk_search"
 * - TODO：联网搜索: tool name = "web_search"
 * - 知识点搜索: tool name = "knowledge_point_search"
 * - 记忆添加: tool name = "memory_add"
 * - 记忆删除: tool name = "memory_delete"
 * - 学习计划批量创建: tool name = "learning_plan_batch_create"
 * - 学习计划更新: tool name = "learning_plan_update"
 * - 学习计划批量删除: tool name = "learning_plan_batch_delete"
 * - 学习计划完成状态更新: tool name = "learning_plan_completion"
 * 说明：
 * - 实现了完整的记忆管理和学习计划管理功能
 * - 其他旧有的示例工具（计算、天气、文件、数据库等）不再作为路由目标
 * - 默认处理逻辑会提示"未知工具类型"，用于兜底与排查配置问题
 * @author LinSanQi
 */
@Slf4j
@RequiredArgsConstructor
@Component("AgentChatToolExecutorImpl")
public class AgentChatToolExecutorImpl implements ToolExecutor<AgentChatExecutionContext> {

    private static final String EMBEDDING_MODEL_ALIAS = "text-embedding-v4";

    private final AgentMemoryMapper agentMemoryMapper;
    private final AgentLearningTaskMapper agentLearningTaskMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final KnowledgePointVersionMapper knowledgePointVersionMapper;
    private final ResourceChunkMapper resourceChunkMapper;
    private final AiCoreService aiCoreService;
    private final VectorizationTool vectorizationTool;

    /**
     * 执行AI模型请求的工具（带上下文）
     *
     * @param toolExecutionRequest 工具执行请求
     * @param context 工具执行上下文
     * @return 工具执行结果消息
     */
    @Override
    public ToolExecutionResult execute(ToolExecutionRequest toolExecutionRequest, AgentChatExecutionContext context) {
        try {
            String toolName = toolExecutionRequest.name();
            String arguments = toolExecutionRequest.arguments();

            log.info("执行工具: {}, 参数: {}", toolName, arguments);

            String result = executeToolByName(toolName, arguments, context);
            return ToolExecutionResult.success(toolExecutionRequest, result);
        } catch (Exception e) {
            log.error("工具执行失败: {}", e.getMessage(), e);
            return ToolExecutionResult.error(toolExecutionRequest, "工具执行失败: " + e.getMessage());
        }
    }

    /**
     * 根据工具名称执行具体的工具逻辑（十种实际可用工具的集中路由）
     *
     * @param toolName 工具名称
     * @param arguments 工具参数
     * @param context 工具执行上下文
     * @return 执行结果
     */
    private String executeToolByName(String toolName, String arguments, AgentChatExecutionContext context) {
        return switch (toolName) {
            case "semantic_search" -> semanticSearch(arguments, context);
            case "knowledge_point_search" -> knowledgePointSearch(arguments, context);
            case "resource_chunk_search" -> resourceChunkSearch(arguments, context);
            case "memory_add" -> memoryAdd(arguments, context);
            case "memory_delete" -> memoryDelete(arguments, context);
            case "learning_plan_batch_create" -> learningPlanBatchCreate(arguments, context);
            case "learning_plan_update" -> learningPlanUpdate(arguments, context);
            case "learning_plan_batch_delete" -> learningPlanBatchDelete(arguments, context);
            case "learning_plan_completion" -> learningPlanCompletion(arguments, context);
            default -> {
                log.warn("未知工具类型: {}", toolName);
                yield "未知工具类型: " + toolName;
            }
        };
    }

    /**
     * 记忆添加工具
     *
     * @param arguments JSON格式的记忆参数
     * @param context 执行上下文
     * @return 添加结果
     */
    private String memoryAdd(String arguments, AgentChatExecutionContext context) {
        try {
            MemoryAddParams params = JSONUtil.toBean(arguments, MemoryAddParams.class);

            // 创建记忆实体
            AgentMemoryEntity memory = new AgentMemoryEntity();
            memory.setAmId(HutoolSnowflakeIdGenerator.generateLongId());
            memory.setAmUserId(context.getUserId());
            memory.setAmLearningSpaceId(context.getLearningSpaceId());
            memory.setAmSessionId(context.getSessionId());
            // 默认会话级记忆
            memory.setAmLevel(AgentChatMemoryLevelEnum.SESSION.getCode());
            memory.setAmTitle(params.getTitle());
            memory.setAmContent(params.getContent());
            memory.setAmImportanceScore(params.getImportanceScore());
            memory.setAmSource(AgentMemorySourceEnum.CHAT.getCode());
            memory.setAmAccessCount(0);
            memory.setAmCreatedAt(LocalDateTime.now());
            memory.setAmUpdatedAt(LocalDateTime.now());

            // 保存到数据库
            agentMemoryMapper.save(memory);

            ToolExecutionResponse response = ToolExecutionResponse.success("记忆添加成功", memory.getAmId());
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("记忆添加失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("记忆添加失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 记忆删除工具
     *
     * @param arguments JSON格式的删除参数
     * @param context 执行上下文
     * @return 删除结果
     */
    private String memoryDelete(String arguments, AgentChatExecutionContext context) {
        try {
            MemoryDeleteParams params = JSONUtil.toBean(arguments, MemoryDeleteParams.class);

            // 检查记忆是否存在且属于当前用户
            AgentMemoryEntity memory = QueryChain.of(agentMemoryMapper)
                    .select(AgentMemoryEntity::getAmId)
                    .eq(AgentMemoryEntity::getAmId, params.getMemoryId())
                    .eq(AgentMemoryEntity::getAmUserId, context.getUserId())
                    .isNull(AgentMemoryEntity::getAmDeletedAt)
                    .limit(1)
                    .get();

            if (memory == null || memory.getAmId() == null) {
                ToolExecutionResponse response = ToolExecutionResponse.error("记忆不存在或无权限删除，请检查ID是否正确");
                return JSONUtil.toJsonStr(response);
            } else {
                agentMemoryMapper.deleteById(memory.getAmId());
            }

            ToolExecutionResponse response = ToolExecutionResponse.success("记忆删除成功", params.getMemoryId());
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("记忆删除失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("记忆删除失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 学习计划批量创建工具
     *
     * @param arguments JSON格式的创建参数
     * @param context 执行上下文
     * @return 创建结果
     */
    private String learningPlanBatchCreate(String arguments, AgentChatExecutionContext context) {
        try {
            LearningPlanBatchCreateParams params = JSONUtil.toBean(arguments, LearningPlanBatchCreateParams.class);

            LocalDateTime now = LocalDateTime.now();

            List<AgentLearningTaskEntity> tasks = new ArrayList<>();
            List<Long> createdIds = new ArrayList<>();

            params.getItems().forEach(item -> {
                AgentLearningTaskEntity task = new AgentLearningTaskEntity();
                task.setAltId(HutoolSnowflakeIdGenerator.generateLongId());
                task.setAltUserId(context.getUserId());
                task.setAltLearningSpaceId(context.getLearningSpaceId());
                task.setAltSessionId(context.getSessionId());
                task.setAltTitle(item.getTitle());
                task.setAltObjective(item.getObjective());
                task.setAltDifficultyLevel(item.getDifficultyLevel().getCode());
                task.setAltIsCompleted(false);
                task.setAltCreatedAt(now);
                task.setAltUpdatedAt(now);

                tasks.add(task);
                createdIds.add(task.getAltId());
            });

            // 批量保存
            agentLearningTaskMapper.saveBatch(tasks);

            ToolExecutionResponse response = ToolExecutionResponse.success("学习计划批量创建成功", createdIds);
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("学习计划批量创建失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("学习计划批量创建失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 验证学习计划是否存在且属于当前用户
     *
     * @param planId 计划ID
     * @param context 执行上下文
     * @return 是否存在且有权限
     */
    private boolean isLearningTaskNotExist(Long planId, AgentChatExecutionContext context) {
        return QueryChain.of(agentLearningTaskMapper)
                .eq(AgentLearningTaskEntity::getAltId, planId)
                .eq(AgentLearningTaskEntity::getAltUserId, context.getUserId())
                .eq(AgentLearningTaskEntity::getAltLearningSpaceId, context.getLearningSpaceId())
                .eq(AgentLearningTaskEntity::getAltSessionId, context.getSessionId())
                .count() <= 0;
    }

    /**
     * 学习计划更新工具
     *
     * @param arguments JSON格式的更新参数
     * @param context 执行上下文
     * @return 更新结果
     */
    private String learningPlanUpdate(String arguments, AgentChatExecutionContext context) {
        try {
            LearningPlanUpdateParams params = JSONUtil.toBean(arguments, LearningPlanUpdateParams.class);

            // 验证计划是否存在且属于当前用户
            if (isLearningTaskNotExist(params.getPlanId(), context)) {
                ToolExecutionResponse response = ToolExecutionResponse.error("学习计划不存在或无权限修改");
                return JSONUtil.toJsonStr(response);
            }

            // 构建更新链
            UpdateChain updateChain = UpdateChain.of(agentLearningTaskMapper)
                    .eq(AgentLearningTaskEntity::getAltId, params.getPlanId())
                    .set(AgentLearningTaskEntity::getAltUpdatedAt, LocalDateTime.now());

            // 只更新非空字段
            if (params.getTitle() != null) {
                updateChain.set(AgentLearningTaskEntity::getAltTitle, params.getTitle());
            }
            if (params.getObjective() != null) {
                updateChain.set(AgentLearningTaskEntity::getAltObjective, params.getObjective());
            }
            if (params.getDifficultyLevel() != null) {
                updateChain.set(AgentLearningTaskEntity::getAltDifficultyLevel, params.getDifficultyLevel());
            }
            if (params.getCompleted() != null) {
                updateChain.set(AgentLearningTaskEntity::getAltIsCompleted, params.getCompleted());
            }

            updateChain.execute();

            ToolExecutionResponse response = ToolExecutionResponse.success("学习计划更新成功");
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("学习计划更新失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("学习计划更新失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 学习计划批量删除工具
     *
     * @param arguments JSON格式的删除参数
     * @param context 执行上下文
     * @return 删除结果
     */
    private String learningPlanBatchDelete(String arguments, AgentChatExecutionContext context) {
        try {
            LearningPlanBatchDeleteParams params = JSONUtil.toBean(arguments, LearningPlanBatchDeleteParams.class);

            // 检查所有计划是否存在且属于当前用户，只查询ID字段提升性能
            List<AgentLearningTaskEntity> existingTasks = QueryChain.of(agentLearningTaskMapper)
                    .select(AgentLearningTaskEntity::getAltId)
                    .in(AgentLearningTaskEntity::getAltId, params.getPlanIds())
                    .eq(AgentLearningTaskEntity::getAltUserId, context.getUserId())
                    .eq(AgentLearningTaskEntity::getAltLearningSpaceId, context.getLearningSpaceId())
                    .eq(AgentLearningTaskEntity::getAltSessionId, context.getSessionId())
                    .list();

            if (existingTasks.isEmpty()) {
                ToolExecutionResponse response = ToolExecutionResponse.error("没有找到可删除的学习计划");
                return JSONUtil.toJsonStr(response);
            }

            List<Long> existingIds = existingTasks.stream()
                    .map(AgentLearningTaskEntity::getAltId)
                    .collect(Collectors.toList());

            // 批量删除
            agentLearningTaskMapper.deleteByIds(existingIds);

            ToolExecutionResponse response = ToolExecutionResponse.success("学习计划批量删除成功", existingIds);
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("学习计划批量删除失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("学习计划批量删除失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 学习计划完成状态更新工具
     *
     * @param arguments JSON格式的完成状态参数
     * @param context 执行上下文
     * @return 更新结果
     */
    private String learningPlanCompletion(String arguments, AgentChatExecutionContext context) {
        try {
            LearningPlanCompletionParams params = JSONUtil.toBean(arguments, LearningPlanCompletionParams.class);

            // 验证计划是否存在且属于当前用户
            if (isLearningTaskNotExist(params.getPlanId(), context)) {
                ToolExecutionResponse response = ToolExecutionResponse.error("学习计划不存在或无权限修改");
                return JSONUtil.toJsonStr(response);
            }

            // 更新完成状态
            UpdateChain.of(agentLearningTaskMapper)
                    .eq(AgentLearningTaskEntity::getAltId, params.getPlanId())
                    .set(AgentLearningTaskEntity::getAltIsCompleted, params.getIsCompleted())
                    .set(AgentLearningTaskEntity::getAltUpdatedAt, LocalDateTime.now())
                    .execute();

            String message = params.getIsCompleted() ? "学习计划标记为已完成" : "学习计划取消完成状态";
            ToolExecutionResponse response = ToolExecutionResponse.success(message, params.getPlanId());
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("学习计划完成状态更新失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("学习计划完成状态更新失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }
    /**
     * 知识点搜索工具
     * 逻辑：通过主表过滤权限，获取 CurrentVersionId，然后在 Version 表中匹配关键词
     *
     * @param arguments JSON格式的搜索参数
     * @param context   执行上下文
     * @return 搜索结果
     */
    private String knowledgePointSearch(String arguments, AgentChatExecutionContext context) {
        try {
            KnowledgeSearchParams params = JSONUtil.toBean(arguments, KnowledgeSearchParams.class);

            String keyword = params.getKeyword();
            int maxResults = (params.getMaxResults() != null && params.getMaxResults() > 0) ? params.getMaxResults() : 10;
            boolean scopeToSpace = Boolean.TRUE.equals(params.getScopeToLearningSpace());

            if (StrUtil.isBlank(keyword)) {
                return JSONUtil.toJsonStr(ToolExecutionResponse.error("搜索关键词不能为空"));
            }


            List<Long> validVersionIds = QueryChain.of(knowledgePointMapper)
                    .select(KnowledgePointEntity::getKpCurrentVersionId)
                    .eq(KnowledgePointEntity::getKpCreatedByUserId, context.getUserId())
                    .eq(scopeToSpace, KnowledgePointEntity::getKpLearningSpaceId, context.getLearningSpaceId())
                    .isNotNull(KnowledgePointEntity::getKpCurrentVersionId)
                    .list()
                    .stream()
                    .map(KnowledgePointEntity::getKpCurrentVersionId)
                    .collect(Collectors.toList());

            if (CollUtil.isEmpty(validVersionIds)) {
                return JSONUtil.toJsonStr(ToolExecutionResponse.success(
                        "当前空间下没有可搜索的知识点", new ArrayList<>()));
            }


            List<KnowledgePointVersionEntity> resultList = QueryChain.of(knowledgePointVersionMapper)
                    .in(KnowledgePointVersionEntity::getKpvId, validVersionIds)
                    .andNested(wrapper -> wrapper
                            .like(KnowledgePointVersionEntity::getKpvTitle, keyword)
                            .or()
                            .like(KnowledgePointVersionEntity::getKpvDefinition, keyword)
                            .or()
                            .like(KnowledgePointVersionEntity::getKpvExplanation, keyword)
                            .or()
                            .like(KnowledgePointVersionEntity::getKpvExample, keyword)
                            .or()
                            .like(KnowledgePointVersionEntity::getKpvFormulaOrCode, keyword)
                    )
                    .orderByDesc(KnowledgePointVersionEntity::getKpvCreatedAt)
                    .limit(maxResults)
                    .list();

            if (resultList.isEmpty()) {
                return JSONUtil.toJsonStr(ToolExecutionResponse.success(
                        "未找到包含关键词 '" + keyword + "' 的相关知识点", new ArrayList<>()));
            }

            ToolExecutionResponse response = ToolExecutionResponse.success("知识点搜索成功" + resultList);
            return JSONUtil.toJsonStr(response);

        } catch (Exception e) {
            log.error("知识点搜索失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("知识点搜索失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 资料分片搜索工具
     *
     * @param arguments JSON格式的搜索参数
     * @param context   执行上下文
     * @return 搜索结果
     */
    private String resourceChunkSearch(String arguments, AgentChatExecutionContext context) {
        try {
            ResourceChunkSearchParams params = JSONUtil.toBean(arguments, ResourceChunkSearchParams.class);

            String keyword = params.getKeyword();
            int maxResults = (params.getMaxResults() != null && params.getMaxResults() > 0) ? params.getMaxResults() : 10;
            boolean scopeToSpace = Boolean.TRUE.equals(params.getScopeToLearningSpace());
            Long resourceId = params.getResourceId();

            if (StrUtil.isBlank(keyword)) {
                return JSONUtil.toJsonStr(ToolExecutionResponse.error("搜索关键词不能为空"));
            }

            List<ResourceChunkEntity> resultList = QueryChain.of(resourceChunkMapper)
                    .eq(ResourceChunkEntity::getRcCreatedByUserId, context.getUserId())
                    .eq(scopeToSpace, ResourceChunkEntity::getRcLearningSpaceId, context.getLearningSpaceId())
                    .eq(resourceId != null, ResourceChunkEntity::getRcResourceId, resourceId)
                    .andNested(wrapper -> wrapper
                            .like(ResourceChunkEntity::getRcContent, keyword)
                            .or()
                            .like(ResourceChunkEntity::getRcKeywords, keyword)
                    )
                    .orderByDesc(ResourceChunkEntity::getRcCreatedAt)
                    .limit(maxResults)
                    .list();

            if (resultList.isEmpty()) {
                return JSONUtil.toJsonStr(ToolExecutionResponse.success(
                        "未找到包含关键词 '" + keyword + "' 的相关资料分片", new ArrayList<>()));
            }

            ToolExecutionResponse response = ToolExecutionResponse.success("资料分片搜索成功" + resultList);
            return JSONUtil.toJsonStr(response);

        } catch (Exception e) {
            log.error("资料分片搜索失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("资料分片搜索失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 语义搜索工具
     *
     * @param arguments JSON格式的搜索参数
     * @param context   执行上下文
     * @return 搜索结果
     */
    private String semanticSearch(String arguments, AgentChatExecutionContext context) {
        try {
            SemanticSearchParams params = JSONUtil.toBean(arguments, SemanticSearchParams.class);

            String query = params.getQuery();
            int maxResults = (params.getMaxResults() != null && params.getMaxResults() > 0) ? params.getMaxResults() : 10;
            double minScore = (params.getMinScore() != null && params.getMinScore() > 0) ? params.getMinScore() : 0.7;
            boolean scopeToSpace = Boolean.TRUE.equals(params.getScopeToLearningSpace());

            if (StrUtil.isBlank(query)) {
                return JSONUtil.toJsonStr(ToolExecutionResponse.error("搜索查询文本不能为空"));
            }

            // 1. 获取嵌入模型
            EmbeddingModel embeddingModel = aiCoreService.getEmbeddingModel(EMBEDDING_MODEL_ALIAS);

            // 2. 生成查询向量
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            // 3. 构建过滤条件
            Filter userFilter = MetadataFilterBuilder.metadataKey(ResourceVectorizeMetadataKey.USER_ID).isEqualTo(String.valueOf(context.getUserId()));
            Filter filter;
            if (scopeToSpace) {
                Filter spaceFilter = MetadataFilterBuilder.metadataKey(ResourceVectorizeMetadataKey.LEARNING_SPACE_ID).isEqualTo(String.valueOf(context.getLearningSpaceId()));
                filter = new And(userFilter, spaceFilter);
            } else {
                filter = userFilter;
            }

            // 4. 构建搜索请求
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .filter(filter)
                    .build();

            // 5. 执行搜索
            EmbeddingSearchResult<TextSegment> searchResult = vectorizationTool.search(searchRequest, embeddingModel);

            List<String> matches = searchResult.matches().stream()
                    .map(match -> match.embedded().text())
                    .collect(Collectors.toList());

            if (matches.isEmpty()) {
                return JSONUtil.toJsonStr(ToolExecutionResponse.success(
                        "未找到与 '" + query + "' 语义相关的资料", new ArrayList<>()));
            }

            ToolExecutionResponse response = ToolExecutionResponse.success("语义搜索成功: " + matches);
            return JSONUtil.toJsonStr(response);

        } catch (Exception e) {
            log.error("语义搜索失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("语义搜索失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

}

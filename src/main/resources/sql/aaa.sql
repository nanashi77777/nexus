/*
 Navicat Premium Dump SQL

 Source Server         : txy
 Source Server Type    : PostgreSQL
 Source Server Version : 160010 (160010)
 Source Host           : 175.178.106.211:5432
 Source Catalog        : nexus_v1
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 160010 (160010)
 File Encoding         : 65001

 Date: 11/12/2025 12:35:24
*/


-- ----------------------------
-- Table structure for agent_chat_messages
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_chat_messages";
CREATE TABLE "public"."agent_chat_messages" (
  "acm_id" int8 NOT NULL,
  "acm_session_id" int8 NOT NULL,
  "acm_user_id" int8 NOT NULL,
  "acm_learning_space_id" int8 NOT NULL,
  "acm_role" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "acm_content" text COLLATE "pg_catalog"."default",
  "acm_type" int4 NOT NULL DEFAULT 0,
  "acm_correlation_content" text COLLATE "pg_catalog"."default",
  "acm_tokens" int4,
  "acm_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "acm_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "acm_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_id" IS '消息ID';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_session_id" IS '会话ID';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_user_id" IS '用户ID';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_role" IS '消息角色 (USER, ASSISTANT, SYSTEM)';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_content" IS '消息内容';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_type" IS '消息类型 (0: 普通消息, 1：工具请求, 2：工具响应, 3: 工具列表)';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_correlation_content" IS '关联的内容';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_tokens" IS '输入/输出的token数量';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."agent_chat_messages"."acm_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."agent_chat_messages" IS 'Agent聊天消息表';

-- ----------------------------
-- Table structure for agent_chat_sessions
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_chat_sessions";
CREATE TABLE "public"."agent_chat_sessions" (
  "acs_id" int8 NOT NULL,
  "acs_user_id" int8 NOT NULL,
  "acs_learning_space_id" int8 NOT NULL,
  "acs_title" varchar(255) COLLATE "pg_catalog"."default",
  "acs_type" int2 NOT NULL,
  "acs_status" int2 NOT NULL DEFAULT '1'::smallint,
  "acs_belongs_to" int2,
  "acs_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "acs_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "acs_deleted_at" timestamp(6),
  "acs_is_auto_call_tool" int2 NOT NULL
)
;
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_id" IS '会话ID，使用雪花算法生成 (对应 @TableId(value = IdAutoType.NONE))';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_user_id" IS '用户ID (外键, 关联 user_accounts.ua_id)';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_learning_space_id" IS '学习空间ID (外键, 关联 learning_spaces.ls_id)';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_title" IS '会话标题（用户自定义）';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_type" IS '会话类型 (1=CHAT 普通聊天, 2=LEARNING 学习会话)';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_status" IS '会话状态 (1=NORMAL 正常, 2=RESPONDING 响应中, 3=PAUSED 暂停, 4=TOOL_CALLING 工具调用中, 5=ERROR 错误)';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_belongs_to" IS '会话所属 (1=GRAPH 图谱, 2=EXPLANATION 讲解, 3=NOTE 笔记, 4=LEARNING 学习)';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_updated_at" IS '最后修改时间';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_deleted_at" IS '逻辑删除时间，为NULL表示未删除 (对应 @LogicDelete)';
COMMENT ON COLUMN "public"."agent_chat_sessions"."acs_is_auto_call_tool" IS '自动调用工具方式';
COMMENT ON TABLE "public"."agent_chat_sessions" IS 'Agent聊天会话表，用于管理用户的聊天会话';

-- ----------------------------
-- Table structure for agent_learning_tasks
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_learning_tasks";
CREATE TABLE "public"."agent_learning_tasks" (
  "alt_id" int8 NOT NULL,
  "alt_user_id" int8 NOT NULL,
  "alt_learning_space_id" int8 NOT NULL,
  "alt_session_id" int8 NOT NULL,
  "alt_title" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "alt_objective" text COLLATE "pg_catalog"."default",
  "alt_difficulty_level" int2,
  "alt_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "alt_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "alt_deleted_at" timestamp(6),
  "alt_is_completed" bool
)
;
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_id" IS '学习任务ID，使用雪花算法生成';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_user_id" IS '用户ID (外键, 关联 user_accounts.ua_id)';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_session_id" IS '所属会话ID (外键, 关联 agent_chat_sessions.acs_id)';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_title" IS '规划标题';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_objective" IS '学习目标 (TEXT类型)';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_difficulty_level" IS '难度评估 (1=BEGINNER, 2=INTERMEDIATE, 3=ADVANCED, 4=EXPERT)';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_created_at" IS '创建时间 (修正自Java实体的alpCreatedAt)';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_updated_at" IS '最后修改时间 (修正自Java实体的alpUpdatedAt)';
COMMENT ON COLUMN "public"."agent_learning_tasks"."alt_deleted_at" IS '逻辑删除时间，为NULL表示未删除 (修正自Java实体的alpDeletedAt)';
COMMENT ON TABLE "public"."agent_learning_tasks" IS 'Agent学习规划记录表，用于记录学习会话中的学习任务';

-- ----------------------------
-- Table structure for agent_memories
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_memories";
CREATE TABLE "public"."agent_memories" (
  "am_id" int8 NOT NULL,
  "am_user_id" int8 NOT NULL,
  "am_learning_space_id" int8 NOT NULL,
  "am_session_id" int8,
  "am_level" int2 NOT NULL DEFAULT '1'::smallint,
  "am_title" varchar(255) COLLATE "pg_catalog"."default",
  "am_content" text COLLATE "pg_catalog"."default" NOT NULL,
  "am_importance_score" int2 DEFAULT 5,
  "am_tags" jsonb,
  "am_source" varchar(50) COLLATE "pg_catalog"."default",
  "am_access_count" int4 DEFAULT 0,
  "am_last_accessed_at" timestamp(6),
  "am_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "am_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "am_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."agent_memories"."am_id" IS '记忆ID，使用雪花算法生成';
COMMENT ON COLUMN "public"."agent_memories"."am_user_id" IS '用户ID (外键, 关联 user_accounts.ua_id)';
COMMENT ON COLUMN "public"."agent_memories"."am_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."agent_memories"."am_session_id" IS '会话ID (外键, 关联 agent_chat_sessions.acs_id, 可为NULL表示全局)';
COMMENT ON COLUMN "public"."agent_memories"."am_level" IS '记忆等级 (0=不启用, 1=会话级, 2=全局)';
COMMENT ON COLUMN "public"."agent_memories"."am_title" IS '记忆标题';
COMMENT ON COLUMN "public"."agent_memories"."am_content" IS '记忆内容 (TEXT类型)';
COMMENT ON COLUMN "public"."agent_memories"."am_importance_score" IS '重要性评分（1-10，10为最重要）';
COMMENT ON COLUMN "public"."agent_memories"."am_tags" IS '标签列表 (JSONB格式, e.g., ["Java", "核心概念"])';
COMMENT ON COLUMN "public"."agent_memories"."am_source" IS '记忆来源 (chat, learning, manual)';
COMMENT ON COLUMN "public"."agent_memories"."am_access_count" IS '访问次数';
COMMENT ON COLUMN "public"."agent_memories"."am_last_accessed_at" IS '最后访问时间';
COMMENT ON COLUMN "public"."agent_memories"."am_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."agent_memories"."am_updated_at" IS '最后修改时间';
COMMENT ON COLUMN "public"."agent_memories"."am_deleted_at" IS '逻辑删除时间，为NULL表示未删除';
COMMENT ON TABLE "public"."agent_memories" IS '记忆实体表，用于存储全局和会话级的用户相关记忆内容';

-- ----------------------------
-- Table structure for agent_tool_calls
-- ----------------------------
DROP TABLE IF EXISTS "public"."agent_tool_calls";
CREATE TABLE "public"."agent_tool_calls" (
  "atc_id" int8 NOT NULL,
  "atc_message_id" int8 NOT NULL,
  "atc_session_id" int8 NOT NULL,
  "atc_user_id" int8 NOT NULL,
  "atc_learning_space_id" int8 NOT NULL,
  "atc_tool_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "atc_arguments" jsonb,
  "atc_result" text COLLATE "pg_catalog"."default",
  "atc_status" int2 NOT NULL DEFAULT '0'::smallint,
  "atc_error_message" text COLLATE "pg_catalog"."default",
  "atc_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "atc_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "atc_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_id" IS '工具调用ID，使用雪花算法生成';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_message_id" IS '关联消息ID (外键, 关联 agent_chat_messages.acm_id)';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_session_id" IS '会话ID (外键, 关联 agent_chat_sessions.acs_id)';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_user_id" IS '用户ID (外键, 关联 user_accounts.ua_id)';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_tool_name" IS '工具名称';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_arguments" IS '工具调用参数 (JSONB格式)';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_result" IS '工具调用结果 (TEXT类型)';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_status" IS '调用状态 (0=PENDING_AUTHORIZATION, 1=REJECTED, 2=AUTHORIZED, 3=EXECUTING, 4=COMPLETED, 5=ERROR, 6=TIMEOUT)';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_error_message" IS '错误信息 (TEXT类型)';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_updated_at" IS '最后修改时间';
COMMENT ON COLUMN "public"."agent_tool_calls"."atc_deleted_at" IS '逻辑删除时间，为NULL表示未删除';
COMMENT ON TABLE "public"."agent_tool_calls" IS 'Agent工具调用记录表';

-- ----------------------------
-- Table structure for ai_model_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_model_config";
CREATE TABLE "public"."ai_model_config" (
  "amc_id" int8 NOT NULL DEFAULT nextval('ai_model_config_amc_id_seq'::regclass),
  "amc_provider_id" int8 NOT NULL,
  "amc_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "amc_used_for" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "amc_config" jsonb,
  "amc_status" int4 NOT NULL DEFAULT 1,
  "amc_create_time" timestamp(6) NOT NULL DEFAULT now(),
  "amc_update_time" timestamp(6) NOT NULL DEFAULT now(),
  "amc_is_deleted" bool
)
;
COMMENT ON COLUMN "public"."ai_model_config"."amc_id" IS '自增主键';
COMMENT ON COLUMN "public"."ai_model_config"."amc_provider_id" IS '关联的供应商ID';
COMMENT ON COLUMN "public"."ai_model_config"."amc_name" IS '自定义名称';
COMMENT ON COLUMN "public"."ai_model_config"."amc_used_for" IS '模型被用于的模块';
COMMENT ON COLUMN "public"."ai_model_config"."amc_config" IS '模型特定配置JSON';
COMMENT ON COLUMN "public"."ai_model_config"."amc_status" IS '状态：1-启用，2-禁用，3-删除';
COMMENT ON COLUMN "public"."ai_model_config"."amc_create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ai_model_config"."amc_update_time" IS '更新时间';
COMMENT ON TABLE "public"."ai_model_config" IS 'AI模型配置表';

-- ----------------------------
-- Table structure for ai_provider_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_provider_config";
CREATE TABLE "public"."ai_provider_config" (
  "apc_id" int8 NOT NULL DEFAULT nextval('ai_provider_config_apc_id_seq'::regclass),
  "apc_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "apc_official_url" varchar(500) COLLATE "pg_catalog"."default",
  "apc_channel" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "apc_base_url" varchar(500) COLLATE "pg_catalog"."default" NOT NULL,
  "apc_api_key" text COLLATE "pg_catalog"."default" NOT NULL,
  "apc_status" int4 NOT NULL DEFAULT 1,
  "apc_create_time" timestamp(6) NOT NULL DEFAULT now(),
  "apc_update_time" timestamp(6) NOT NULL DEFAULT now(),
  "apc_is_deleted" bool
)
;
COMMENT ON COLUMN "public"."ai_provider_config"."apc_id" IS '自增主键';
COMMENT ON COLUMN "public"."ai_provider_config"."apc_name" IS '供应商名称';
COMMENT ON COLUMN "public"."ai_provider_config"."apc_official_url" IS '供应商官网地址';
COMMENT ON COLUMN "public"."ai_provider_config"."apc_channel" IS '渠道/供应商类型';
COMMENT ON COLUMN "public"."ai_provider_config"."apc_base_url" IS '基础URL';
COMMENT ON COLUMN "public"."ai_provider_config"."apc_api_key" IS 'API密钥（加密存储）';
COMMENT ON COLUMN "public"."ai_provider_config"."apc_status" IS '状态：1-启用，2-禁用，3-删除';
COMMENT ON COLUMN "public"."ai_provider_config"."apc_create_time" IS '创建时间';
COMMENT ON COLUMN "public"."ai_provider_config"."apc_update_time" IS '更新时间';
COMMENT ON TABLE "public"."ai_provider_config" IS 'AI服务商配置表';

-- ----------------------------
-- Table structure for async_tasks
-- ----------------------------
DROP TABLE IF EXISTS "public"."async_tasks";
CREATE TABLE "public"."async_tasks" (
  "at_id" int8 NOT NULL DEFAULT nextval('async_tasks_at_id_seq'::regclass),
  "at_task_type" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "at_status" int4 NOT NULL,
  "at_parameters_json" jsonb,
  "at_result_json" jsonb,
  "at_error_message" text COLLATE "pg_catalog"."default",
  "at_owner_identifier" varchar(255) COLLATE "pg_catalog"."default",
  "at_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "at_started_at" timestamp(6),
  "at_finished_at" timestamp(6),
  "at_user_friendly_message" varchar(255) COLLATE "pg_catalog"."default",
  "at_audit_details" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."async_tasks"."at_id" IS '任务的唯一标识符，自动递增。';
COMMENT ON COLUMN "public"."async_tasks"."at_task_type" IS '任务的类型，对应于一个特定的TaskExecutor Bean。';
COMMENT ON COLUMN "public"."async_tasks"."at_status" IS '任务的当前状态 (例如, WAITING, RUNNING, COMPLETED)。详见TaskStatusEnum。';
COMMENT ON COLUMN "public"."async_tasks"."at_parameters_json" IS '执行任务所需的JSONB编码的参数。';
COMMENT ON COLUMN "public"."async_tasks"."at_result_json" IS '成功完成的任务的JSONB编码的结果。';
COMMENT ON COLUMN "public"."async_tasks"."at_error_message" IS '如果任务失败，则为错误信息或堆栈跟踪。';
COMMENT ON COLUMN "public"."async_tasks"."at_owner_identifier" IS '发起任务的用户或系统组件的标识符。';
COMMENT ON COLUMN "public"."async_tasks"."at_created_at" IS '提交任务时的时间戳。';
COMMENT ON COLUMN "public"."async_tasks"."at_started_at" IS '任务执行开始时的时间戳。';
COMMENT ON COLUMN "public"."async_tasks"."at_finished_at" IS '任务完成（无论是完成、失败还是取消）时的时间戳。';
COMMENT ON TABLE "public"."async_tasks" IS '用于存储和管理由系统处理的异步任务的元数据和状态。';

-- ----------------------------
-- Table structure for explanation_documents
-- ----------------------------
DROP TABLE IF EXISTS "public"."explanation_documents";
CREATE TABLE "public"."explanation_documents" (
  "ed_id" int8 NOT NULL,
  "ed_learning_space_id" int8 NOT NULL,
  "ed_created_by_user_id" int8 NOT NULL,
  "ed_title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "ed_description" text COLLATE "pg_catalog"."default",
  "ed_status" int2 NOT NULL DEFAULT 0,
  "ed_section_order" jsonb DEFAULT '[]'::jsonb,
  "ed_graph_config" jsonb DEFAULT '{}'::jsonb,
  "ed_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "ed_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "ed_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."explanation_documents"."ed_id" IS '讲解文档ID';
COMMENT ON COLUMN "public"."explanation_documents"."ed_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."explanation_documents"."ed_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."explanation_documents"."ed_title" IS '讲解文档标题';
COMMENT ON COLUMN "public"."explanation_documents"."ed_description" IS '讲解文档描述';
COMMENT ON COLUMN "public"."explanation_documents"."ed_status" IS '文档状态 (0-草稿、1-AI生成中、2-正常)';
COMMENT ON COLUMN "public"."explanation_documents"."ed_section_order" IS '章节位置顺序';
COMMENT ON COLUMN "public"."explanation_documents"."ed_graph_config" IS '图谱数据配置';
COMMENT ON COLUMN "public"."explanation_documents"."ed_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."explanation_documents"."ed_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."explanation_documents"."ed_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."explanation_documents" IS '讲解文档表';

-- ----------------------------
-- Table structure for explanation_points
-- ----------------------------
DROP TABLE IF EXISTS "public"."explanation_points";
CREATE TABLE "public"."explanation_points" (
  "ep_id" int8 NOT NULL,
  "ep_explanation_document_id" int8 NOT NULL,
  "ep_created_by_user_id" int8 NOT NULL,
  "ep_title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "ep_definition" text COLLATE "pg_catalog"."default",
  "ep_explanation" text COLLATE "pg_catalog"."default",
  "ep_formula_or_code" text COLLATE "pg_catalog"."default",
  "ep_example" text COLLATE "pg_catalog"."default",
  "ep_style_config" jsonb DEFAULT '{}'::jsonb,
  "ep_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "ep_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "ep_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."explanation_points"."ep_id" IS '知识点ID';
COMMENT ON COLUMN "public"."explanation_points"."ep_explanation_document_id" IS '所属讲解文档ID';
COMMENT ON COLUMN "public"."explanation_points"."ep_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."explanation_points"."ep_title" IS '知识点标题';
COMMENT ON COLUMN "public"."explanation_points"."ep_definition" IS '知识点定义';
COMMENT ON COLUMN "public"."explanation_points"."ep_explanation" IS '知识点解释';
COMMENT ON COLUMN "public"."explanation_points"."ep_formula_or_code" IS '公式或代码';
COMMENT ON COLUMN "public"."explanation_points"."ep_example" IS '使用示例';
COMMENT ON COLUMN "public"."explanation_points"."ep_style_config" IS '节点样式配置';
COMMENT ON COLUMN "public"."explanation_points"."ep_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."explanation_points"."ep_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."explanation_points"."ep_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."explanation_points" IS '讲解知识点表';

-- ----------------------------
-- Table structure for explanation_relations
-- ----------------------------
DROP TABLE IF EXISTS "public"."explanation_relations";
CREATE TABLE "public"."explanation_relations" (
  "er_id" int8 NOT NULL,
  "er_explanation_document_id" int8 NOT NULL,
  "er_created_by_user_id" int8 NOT NULL,
  "er_source_point_id" int8 NOT NULL,
  "er_target_point_id" int8 NOT NULL,
  "er_relation_type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "er_description" text COLLATE "pg_catalog"."default",
  "er_style_config" jsonb DEFAULT '{}'::jsonb,
  "er_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "er_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "er_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."explanation_relations"."er_id" IS '关系ID';
COMMENT ON COLUMN "public"."explanation_relations"."er_explanation_document_id" IS '所属讲解文档ID';
COMMENT ON COLUMN "public"."explanation_relations"."er_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."explanation_relations"."er_source_point_id" IS '源知识点ID';
COMMENT ON COLUMN "public"."explanation_relations"."er_target_point_id" IS '目标知识点ID';
COMMENT ON COLUMN "public"."explanation_relations"."er_relation_type" IS '关系类型';
COMMENT ON COLUMN "public"."explanation_relations"."er_description" IS '关系描述';
COMMENT ON COLUMN "public"."explanation_relations"."er_style_config" IS '边样式配置';
COMMENT ON COLUMN "public"."explanation_relations"."er_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."explanation_relations"."er_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."explanation_relations"."er_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."explanation_relations" IS '讲解关系表';

-- ----------------------------
-- Table structure for explanation_sections
-- ----------------------------
DROP TABLE IF EXISTS "public"."explanation_sections";
CREATE TABLE "public"."explanation_sections" (
  "es_id" int8 NOT NULL,
  "es_title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "es_summary" text COLLATE "pg_catalog"."default",
  "es_content" text COLLATE "pg_catalog"."default",
  "es_created_by_user_id" int8 NOT NULL,
  "es_explanation_document_id" int8 NOT NULL,
  "es_subsection_order" jsonb DEFAULT '[]'::jsonb,
  "es_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "es_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "es_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."explanation_sections"."es_id" IS '章节ID';
COMMENT ON COLUMN "public"."explanation_sections"."es_title" IS '章节标题';
COMMENT ON COLUMN "public"."explanation_sections"."es_summary" IS '章节摘要';
COMMENT ON COLUMN "public"."explanation_sections"."es_content" IS '章节内容';
COMMENT ON COLUMN "public"."explanation_sections"."es_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."explanation_sections"."es_explanation_document_id" IS '所属讲解文档ID';
COMMENT ON COLUMN "public"."explanation_sections"."es_subsection_order" IS '小节位置顺序';
COMMENT ON COLUMN "public"."explanation_sections"."es_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."explanation_sections"."es_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."explanation_sections"."es_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."explanation_sections" IS '讲解章节表';

-- ----------------------------
-- Table structure for explanation_subsections
-- ----------------------------
DROP TABLE IF EXISTS "public"."explanation_subsections";
CREATE TABLE "public"."explanation_subsections" (
  "ess_id" int8 NOT NULL,
  "ess_title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "ess_summary" text COLLATE "pg_catalog"."default",
  "ess_content" text COLLATE "pg_catalog"."default",
  "ess_created_by_user_id" int8 NOT NULL,
  "ess_explanation_document_id" int8 NOT NULL,
  "ess_section_id" int8 NOT NULL,
  "ess_order" int4 NOT NULL DEFAULT 0,
  "ess_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "ess_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "ess_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."explanation_subsections"."ess_id" IS '小节ID';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_title" IS '小节标题';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_summary" IS '小节摘要';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_content" IS '小节内容';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_explanation_document_id" IS '所属讲解文档ID';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_section_id" IS '所属章节ID';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_order" IS '小节顺序';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."explanation_subsections"."ess_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."explanation_subsections" IS '讲解小节表';

-- ----------------------------
-- Table structure for graph_edges
-- ----------------------------
DROP TABLE IF EXISTS "public"."graph_edges";
CREATE TABLE "public"."graph_edges" (
  "ge_id" int8 NOT NULL,
  "ge_learning_space_id" int8 NOT NULL,
  "ge_created_by_user_id" int8 NOT NULL,
  "ge_graph_id" int8 NOT NULL,
  "ge_source_virtual_node_id" int8,
  "ge_target_virtual_node_id" int8,
  "ge_source_projection_node_id" int8,
  "ge_target_projection_node_id" int8,
  "ge_is_projection" bool NOT NULL DEFAULT false,
  "ge_relation_entity_relation_id" int8,
  "ge_relation_type" varchar(50) COLLATE "pg_catalog"."default",
  "ge_description" text COLLATE "pg_catalog"."default",
  "ge_style_config" jsonb NOT NULL DEFAULT '{}'::jsonb,
  "ge_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "ge_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "ge_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."graph_edges"."ge_id" IS '边ID';
COMMENT ON COLUMN "public"."graph_edges"."ge_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."graph_edges"."ge_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."graph_edges"."ge_graph_id" IS '所属图谱ID';
COMMENT ON COLUMN "public"."graph_edges"."ge_source_virtual_node_id" IS '源虚体节点ID';
COMMENT ON COLUMN "public"."graph_edges"."ge_target_virtual_node_id" IS '目标虚体节点ID';
COMMENT ON COLUMN "public"."graph_edges"."ge_source_projection_node_id" IS '源投影节点ID';
COMMENT ON COLUMN "public"."graph_edges"."ge_target_projection_node_id" IS '目标投影节点ID';
COMMENT ON COLUMN "public"."graph_edges"."ge_is_projection" IS '是否为投影体';
COMMENT ON COLUMN "public"."graph_edges"."ge_relation_entity_relation_id" IS '关联的知识点关系实体ID（投影体时不为null）';
COMMENT ON COLUMN "public"."graph_edges"."ge_relation_type" IS '关系类型（知识点关系实体表字段）';
COMMENT ON COLUMN "public"."graph_edges"."ge_description" IS '关系描述（知识点关系实体表字段）';
COMMENT ON COLUMN "public"."graph_edges"."ge_style_config" IS '边样式配置';
COMMENT ON COLUMN "public"."graph_edges"."ge_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."graph_edges"."ge_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."graph_edges"."ge_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."graph_edges" IS '图谱边表';

-- ----------------------------
-- Table structure for graph_nodes
-- ----------------------------
DROP TABLE IF EXISTS "public"."graph_nodes";
CREATE TABLE "public"."graph_nodes" (
  "gn_id" int8 NOT NULL,
  "gn_learning_space_id" int8 NOT NULL,
  "gn_created_by_user_id" int8 NOT NULL,
  "gn_graph_id" int8 NOT NULL,
  "gn_is_projection" bool NOT NULL DEFAULT false,
  "gn_entity_id" int8,
  "gn_title" varchar(200) COLLATE "pg_catalog"."default",
  "gn_definition" text COLLATE "pg_catalog"."default",
  "gn_explanation" text COLLATE "pg_catalog"."default",
  "gn_formula_or_code" text COLLATE "pg_catalog"."default",
  "gn_example" text COLLATE "pg_catalog"."default",
  "gn_style_config" jsonb NOT NULL DEFAULT '{}'::jsonb,
  "gn_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "gn_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "gn_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."graph_nodes"."gn_id" IS '节点ID';
COMMENT ON COLUMN "public"."graph_nodes"."gn_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."graph_nodes"."gn_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."graph_nodes"."gn_graph_id" IS '所属图谱ID';
COMMENT ON COLUMN "public"."graph_nodes"."gn_is_projection" IS '是否为投影体';
COMMENT ON COLUMN "public"."graph_nodes"."gn_entity_id" IS '关联的知识点实体ID';
COMMENT ON COLUMN "public"."graph_nodes"."gn_title" IS '节点标题（知识点实体表字段）';
COMMENT ON COLUMN "public"."graph_nodes"."gn_definition" IS '节点定义（知识点实体表字段）';
COMMENT ON COLUMN "public"."graph_nodes"."gn_explanation" IS '节点解释（知识点实体表字段）';
COMMENT ON COLUMN "public"."graph_nodes"."gn_formula_or_code" IS '节点公式或代码（知识点实体表字段）';
COMMENT ON COLUMN "public"."graph_nodes"."gn_example" IS '节点示例（知识点实体表字段）';
COMMENT ON COLUMN "public"."graph_nodes"."gn_style_config" IS '节点样式配置';
COMMENT ON COLUMN "public"."graph_nodes"."gn_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."graph_nodes"."gn_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."graph_nodes"."gn_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."graph_nodes" IS '图谱节点表';

-- ----------------------------
-- Table structure for infra_file_metadata
-- ----------------------------
DROP TABLE IF EXISTS "public"."infra_file_metadata";
CREATE TABLE "public"."infra_file_metadata" (
  "fm_id" int8 NOT NULL DEFAULT nextval('infra_file_metadata_fm_id_seq'::regclass),
  "fm_storage_path" varchar(512) COLLATE "pg_catalog"."default" NOT NULL,
  "fm_owner_identifier" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "fm_access_level" int2 NOT NULL DEFAULT 1,
  "fm_original_filename" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "fm_file_size" int8 NOT NULL,
  "fm_mime_type" varchar(128) COLLATE "pg_catalog"."default",
  "fm_provider" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "fm_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "fm_grantees" text[] COLLATE "pg_catalog"."default" DEFAULT '{}'::text[]
)
;
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_id" IS '文件唯一ID (自增生成)';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_storage_path" IS '文件在物理存储中的相对路径';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_owner_identifier" IS '文件所有者的唯一标识';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_access_level" IS '访问级别代码 (0: PRIVATE, 1: PUBLIC)';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_original_filename" IS '原始文件名';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_file_size" IS '文件大小（字节）';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_mime_type" IS 'MIME类型';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_provider" IS '存储提供商（例如：local, oss）';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."infra_file_metadata"."fm_grantees" IS '被授权访问该文件的用户标识列表';
COMMENT ON TABLE "public"."infra_file_metadata" IS '文件元数据表';

-- ----------------------------
-- Table structure for knowledge_folders
-- ----------------------------
DROP TABLE IF EXISTS "public"."knowledge_folders";
CREATE TABLE "public"."knowledge_folders" (
  "kf_id" int8 NOT NULL,
  "kf_learning_space_id" int8 NOT NULL,
  "kf_created_by_user_id" int8 NOT NULL,
  "kf_parent_id" int8,
  "kf_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "kf_level" int2 NOT NULL DEFAULT 0,
  "kf_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "kf_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "kf_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."knowledge_folders"."kf_id" IS '文件夹ID';
COMMENT ON COLUMN "public"."knowledge_folders"."kf_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."knowledge_folders"."kf_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."knowledge_folders"."kf_parent_id" IS '父文件夹ID';
COMMENT ON COLUMN "public"."knowledge_folders"."kf_name" IS '文件夹名称';
COMMENT ON COLUMN "public"."knowledge_folders"."kf_level" IS '文件夹层级（0-5，0为根目录，最高为5级）';
COMMENT ON COLUMN "public"."knowledge_folders"."kf_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."knowledge_folders"."kf_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."knowledge_folders"."kf_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."knowledge_folders" IS '知识点文件夹表';

-- ----------------------------
-- Table structure for knowledge_graphs
-- ----------------------------
DROP TABLE IF EXISTS "public"."knowledge_graphs";
CREATE TABLE "public"."knowledge_graphs" (
  "kg_id" int8 NOT NULL,
  "kg_learning_space_id" int8 NOT NULL,
  "kg_created_by_user_id" int8 NOT NULL,
  "kg_title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "kg_description" text COLLATE "pg_catalog"."default",
  "kg_thumbnail_url" varchar(500) COLLATE "pg_catalog"."default",
  "kg_graph_config_data" jsonb NOT NULL DEFAULT '{}'::jsonb,
  "kg_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "kg_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "kg_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_id" IS '图谱ID';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_title" IS '图谱标题';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_description" IS '图谱描述';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_thumbnail_url" IS '缩略图URL';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_graph_config_data" IS '图谱数据配置';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."knowledge_graphs"."kg_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."knowledge_graphs" IS '知识图谱表';

-- ----------------------------
-- Table structure for knowledge_point_relations
-- ----------------------------
DROP TABLE IF EXISTS "public"."knowledge_point_relations";
CREATE TABLE "public"."knowledge_point_relations" (
  "kpr_id" int8 NOT NULL,
  "kpr_learning_space_id" int8 NOT NULL,
  "kpr_created_by_user_id" int8 NOT NULL,
  "kpr_source_point_id" int8 NOT NULL,
  "kpr_target_point_id" int8 NOT NULL,
  "kpr_relation_type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "kpr_description" text COLLATE "pg_catalog"."default",
  "kpr_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "kpr_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "kpr_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_id" IS '关系ID';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_source_point_id" IS '源知识点ID';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_target_point_id" IS '目标知识点ID';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_relation_type" IS '关系类型';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_description" IS '关系描述';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."knowledge_point_relations"."kpr_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."knowledge_point_relations" IS '知识点关系表';

-- ----------------------------
-- Table structure for knowledge_point_versions
-- ----------------------------
DROP TABLE IF EXISTS "public"."knowledge_point_versions";
CREATE TABLE "public"."knowledge_point_versions" (
  "kpv_id" int8 NOT NULL,
  "kpv_knowledge_point_id" int8 NOT NULL,
  "kpv_created_by_user_id" int8 NOT NULL,
  "kpv_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "kpv_deleted_at" timestamp(6),
  "kpv_title" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "kpv_definition" text COLLATE "pg_catalog"."default",
  "kpv_explanation" text COLLATE "pg_catalog"."default",
  "kpv_formula_or_code" text COLLATE "pg_catalog"."default",
  "kpv_example" text COLLATE "pg_catalog"."default",
  "kpv_difficulty" numeric(3,2)
)
;
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_id" IS '版本ID';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_knowledge_point_id" IS '关联知识点ID';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_deleted_at" IS '删除时间';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_title" IS '知识点标题';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_definition" IS '知识点定义';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_explanation" IS '知识点讲解';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_formula_or_code" IS '公式或代码示例';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_example" IS '示例';
COMMENT ON COLUMN "public"."knowledge_point_versions"."kpv_difficulty" IS '难度系数';
COMMENT ON TABLE "public"."knowledge_point_versions" IS '知识点版本表';

-- ----------------------------
-- Table structure for knowledge_points
-- ----------------------------
DROP TABLE IF EXISTS "public"."knowledge_points";
CREATE TABLE "public"."knowledge_points" (
  "kp_id" int8 NOT NULL,
  "kp_learning_space_id" int8 NOT NULL,
  "kp_created_by_user_id" int8 NOT NULL,
  "kp_folder_id" int8,
  "kp_current_version_id" int8,
  "kp_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "kp_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "kp_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."knowledge_points"."kp_id" IS '知识点ID';
COMMENT ON COLUMN "public"."knowledge_points"."kp_learning_space_id" IS '学习空间ID';
COMMENT ON COLUMN "public"."knowledge_points"."kp_created_by_user_id" IS '创建者用户ID';
COMMENT ON COLUMN "public"."knowledge_points"."kp_folder_id" IS '文件夹ID';
COMMENT ON COLUMN "public"."knowledge_points"."kp_current_version_id" IS '当前版本ID';
COMMENT ON COLUMN "public"."knowledge_points"."kp_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."knowledge_points"."kp_updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."knowledge_points"."kp_deleted_at" IS '删除时间';
COMMENT ON TABLE "public"."knowledge_points" IS '知识点表';

-- ----------------------------
-- Table structure for learning_spaces
-- ----------------------------
DROP TABLE IF EXISTS "public"."learning_spaces";
CREATE TABLE "public"."learning_spaces" (
  "ls_id" int8 NOT NULL,
  "ls_user_id" int8 NOT NULL,
  "ls_name" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "ls_description" varchar(512) COLLATE "pg_catalog"."default",
  "ls_space_prompt" text COLLATE "pg_catalog"."default",
  "ls_cover_image_url" varchar(255) COLLATE "pg_catalog"."default",
  "ls_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "ls_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "ls_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."learning_spaces"."ls_id" IS '主键, 学习空间雪花ID';
COMMENT ON COLUMN "public"."learning_spaces"."ls_user_id" IS '所属用户的ID (外键)';
COMMENT ON COLUMN "public"."learning_spaces"."ls_name" IS '学习空间的名称';
COMMENT ON COLUMN "public"."learning_spaces"."ls_description" IS '对学习空间的详细描述';
COMMENT ON COLUMN "public"."learning_spaces"."ls_space_prompt" IS '空间内AI的全局参考Prompt';
COMMENT ON COLUMN "public"."learning_spaces"."ls_cover_image_url" IS '空间封面图URL, 可为NULL';
COMMENT ON COLUMN "public"."learning_spaces"."ls_created_at" IS '创建时间, 默认 NOW()';
COMMENT ON COLUMN "public"."learning_spaces"."ls_updated_at" IS '最后修改时间, 默认 NOW()';
COMMENT ON COLUMN "public"."learning_spaces"."ls_deleted_at" IS '逻辑删除时间, 为 NULL 表示未删除';
COMMENT ON TABLE "public"."learning_spaces" IS '学习空间表，实现多租户数据隔离的核心。用户的每一份独立学习内容都对应一个学习空间。';

-- ----------------------------
-- Table structure for resource_chunks
-- ----------------------------
DROP TABLE IF EXISTS "public"."resource_chunks";
CREATE TABLE "public"."resource_chunks" (
  "rc_id" int8 NOT NULL,
  "rc_resource_id" int8 NOT NULL,
  "rc_learning_space_id" int8 NOT NULL,
  "rc_created_by_user_id" int8 NOT NULL,
  "rc_content" text COLLATE "pg_catalog"."default",
  "rc_page_index" int4,
  "rc_chunk_index" int4,
  "rc_token_count" int4,
  "rc_vector_id" varchar(255) COLLATE "pg_catalog"."default",
  "rc_vector_dimension" int4,
  "rc_is_vectorized" bool DEFAULT false,
  "rc_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "rc_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "rc_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."resource_chunks"."rc_id" IS '主键, 分片雪花ID';
COMMENT ON COLUMN "public"."resource_chunks"."rc_resource_id" IS '所属资源的ID (外键)';
COMMENT ON COLUMN "public"."resource_chunks"."rc_learning_space_id" IS '所属学习空间的ID (外键)';
COMMENT ON COLUMN "public"."resource_chunks"."rc_created_by_user_id" IS '所属用户的ID (外键)';
COMMENT ON COLUMN "public"."resource_chunks"."rc_content" IS '分片的文本内容';
COMMENT ON COLUMN "public"."resource_chunks"."rc_page_index" IS '所在的页码索引';
COMMENT ON COLUMN "public"."resource_chunks"."rc_chunk_index" IS '在页内的分片顺序索引';
COMMENT ON COLUMN "public"."resource_chunks"."rc_token_count" IS '预估的Token数量';
COMMENT ON COLUMN "public"."resource_chunks"."rc_vector_id" IS '关联的向量数据库中的ID, 可为NULL';
COMMENT ON COLUMN "public"."resource_chunks"."rc_vector_dimension" IS '向量维度';
COMMENT ON COLUMN "public"."resource_chunks"."rc_is_vectorized" IS '标记是否已向量化, 默认 FALSE';
COMMENT ON COLUMN "public"."resource_chunks"."rc_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."resource_chunks"."rc_updated_at" IS '最后修改时间';
COMMENT ON COLUMN "public"."resource_chunks"."rc_deleted_at" IS '逻辑删除时间, 为NULL表示未删除';
COMMENT ON TABLE "public"."resource_chunks" IS '资源分片表，存储从原始资源中拆分出来的最小文本单元';

-- ----------------------------
-- Table structure for resources
-- ----------------------------
DROP TABLE IF EXISTS "public"."resources";
CREATE TABLE "public"."resources" (
  "rs_id" int8 NOT NULL,
  "rs_created_by_user_id" int8 NOT NULL,
  "rs_learning_space_id" int8 NOT NULL,
  "rs_title" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "rs_description" varchar(500) COLLATE "pg_catalog"."default",
  "rs_source_type" int2 NOT NULL,
  "rs_source_uri" varchar(512) COLLATE "pg_catalog"."default",
  "rs_prompt" varchar(2048) COLLATE "pg_catalog"."default",
  "rs_status" int2 NOT NULL DEFAULT 0,
  "rs_parse_error_message" text COLLATE "pg_catalog"."default",
  "rs_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "rs_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "rs_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."resources"."rs_id" IS '主键, 资源雪花ID';
COMMENT ON COLUMN "public"."resources"."rs_created_by_user_id" IS '所属用户的ID';
COMMENT ON COLUMN "public"."resources"."rs_learning_space_id" IS '所属学习空间的ID';
COMMENT ON COLUMN "public"."resources"."rs_title" IS '资源标题';
COMMENT ON COLUMN "public"."resources"."rs_description" IS '资源描述（用于提供给后续AI进行检索的）';
COMMENT ON COLUMN "public"."resources"."rs_source_type" IS '来源类型 (0-UPLOAD, 1-LINK, 2-AI_GENERATED, 3-MANUAL)';
COMMENT ON COLUMN "public"."resources"."rs_source_uri" IS '来源URI (文件路径, URL等)';
COMMENT ON COLUMN "public"."resources"."rs_prompt" IS '用于向模型请求的提示';
COMMENT ON COLUMN "public"."resources"."rs_status" IS '资源状态 (0-PENDING_PARSE, 1-PARSING, 2-PARSE_COMPLETED, 3-PARSE_FAILED)';
COMMENT ON COLUMN "public"."resources"."rs_parse_error_message" IS '解析失败时的错误信息';
COMMENT ON COLUMN "public"."resources"."rs_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."resources"."rs_updated_at" IS '最后修改时间';
COMMENT ON COLUMN "public"."resources"."rs_deleted_at" IS '逻辑删除时间, 为NULL表示未删除';
COMMENT ON TABLE "public"."resources" IS '资源表，存储用户上传或导入的原始资料';

-- ----------------------------
-- Table structure for user_accounts
-- ----------------------------
DROP TABLE IF EXISTS "public"."user_accounts";
CREATE TABLE "public"."user_accounts" (
  "ua_id" int8 NOT NULL,
  "ua_username" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "ua_email" varchar(255) COLLATE "pg_catalog"."default",
  "ua_phone" varchar(20) COLLATE "pg_catalog"."default",
  "ua_password" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "ua_status" int2 NOT NULL DEFAULT '1'::smallint,
  "ua_invite_code" varchar(10) COLLATE "pg_catalog"."default" NOT NULL,
  "ua_inviter_id" int8,
  "ua_created_at" timestamp(6) NOT NULL DEFAULT now(),
  "ua_updated_at" timestamp(6) NOT NULL DEFAULT now(),
  "ua_deleted_at" timestamp(6)
)
;
COMMENT ON COLUMN "public"."user_accounts"."ua_id" IS '用户ID，使用雪花算法生成';
COMMENT ON COLUMN "public"."user_accounts"."ua_username" IS '用户名，3-16个字符，仅允许字母、数字、下划线和连字符';
COMMENT ON COLUMN "public"."user_accounts"."ua_email" IS '电子邮箱地址';
COMMENT ON COLUMN "public"."user_accounts"."ua_phone" IS '用户手机号';
COMMENT ON COLUMN "public"."user_accounts"."ua_password" IS '加密后的用户密码';
COMMENT ON COLUMN "public"."user_accounts"."ua_status" IS '用户状态 (1=ACTIVE正常, 2=BANNED禁用, 0=PENDING待验证)';
COMMENT ON COLUMN "public"."user_accounts"."ua_invite_code" IS '用户专属邀请码';
COMMENT ON COLUMN "public"."user_accounts"."ua_inviter_id" IS '邀请人的用户ID';
COMMENT ON COLUMN "public"."user_accounts"."ua_created_at" IS '创建时间';
COMMENT ON COLUMN "public"."user_accounts"."ua_updated_at" IS '最后修改时间';
COMMENT ON COLUMN "public"."user_accounts"."ua_deleted_at" IS '逻辑删除时间，为NULL表示未删除';
COMMENT ON TABLE "public"."user_accounts" IS '用户基础表，存储用户的核心账户信息';

-- ----------------------------
-- Table structure for vector_store_1024
-- ----------------------------
DROP TABLE IF EXISTS "public"."vector_store_1024";
CREATE TABLE "public"."vector_store_1024" (
  "embedding_id" uuid NOT NULL,
  "embedding" "public"."vector",
  "text" text COLLATE "pg_catalog"."default",
  "metadata" json
)
;

-- ----------------------------
-- Table structure for vector_store_1536
-- ----------------------------
DROP TABLE IF EXISTS "public"."vector_store_1536";
CREATE TABLE "public"."vector_store_1536" (
  "embedding_id" uuid NOT NULL,
  "embedding" "public"."vector",
  "text" text COLLATE "pg_catalog"."default",
  "metadata" json
)
;

-- ----------------------------
-- Table structure for vector_store_2048
-- ----------------------------
DROP TABLE IF EXISTS "public"."vector_store_2048";
CREATE TABLE "public"."vector_store_2048" (
  "embedding_id" uuid NOT NULL,
  "embedding" "public"."vector",
  "text" text COLLATE "pg_catalog"."default",
  "metadata" json
)
;

-- ----------------------------
-- Indexes structure for table agent_chat_messages
-- ----------------------------
CREATE INDEX "idx_acm_learning_space_id" ON "public"."agent_chat_messages" USING btree (
  "acm_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_acm_session_id" ON "public"."agent_chat_messages" USING btree (
  "acm_session_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_acm_type" ON "public"."agent_chat_messages" USING btree (
  "acm_type" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_acm_user_id" ON "public"."agent_chat_messages" USING btree (
  "acm_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_acm_user_session" ON "public"."agent_chat_messages" USING btree (
  "acm_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "acm_session_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_chat_messages
-- ----------------------------
ALTER TABLE "public"."agent_chat_messages" ADD CONSTRAINT "agent_chat_messages_pkey" PRIMARY KEY ("acm_id");

-- ----------------------------
-- Indexes structure for table agent_chat_sessions
-- ----------------------------
CREATE INDEX "idx_acs_created_at" ON "public"."agent_chat_sessions" USING btree (
  "acs_created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_acs_learning_space_id" ON "public"."agent_chat_sessions" USING btree (
  "acs_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_acs_status" ON "public"."agent_chat_sessions" USING btree (
  "acs_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_acs_type" ON "public"."agent_chat_sessions" USING btree (
  "acs_type" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_acs_user_id" ON "public"."agent_chat_sessions" USING btree (
  "acs_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_chat_sessions
-- ----------------------------
ALTER TABLE "public"."agent_chat_sessions" ADD CONSTRAINT "agent_chat_sessions_pkey" PRIMARY KEY ("acs_id");

-- ----------------------------
-- Indexes structure for table agent_learning_tasks
-- ----------------------------
CREATE INDEX "idx_alt_created_at" ON "public"."agent_learning_tasks" USING btree (
  "alt_created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_alt_learning_space_id" ON "public"."agent_learning_tasks" USING btree (
  "alt_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_alt_session_id" ON "public"."agent_learning_tasks" USING btree (
  "alt_session_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_alt_user_id" ON "public"."agent_learning_tasks" USING btree (
  "alt_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_learning_tasks
-- ----------------------------
ALTER TABLE "public"."agent_learning_tasks" ADD CONSTRAINT "agent_learning_tasks_pkey" PRIMARY KEY ("alt_id");

-- ----------------------------
-- Indexes structure for table agent_memories
-- ----------------------------
CREATE INDEX "idx_am_created_at" ON "public"."agent_memories" USING btree (
  "am_created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_am_importance_score" ON "public"."agent_memories" USING btree (
  "am_importance_score" "pg_catalog"."int2_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_am_last_accessed_at" ON "public"."agent_memories" USING btree (
  "am_last_accessed_at" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_am_learning_space_id" ON "public"."agent_memories" USING btree (
  "am_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_am_level" ON "public"."agent_memories" USING btree (
  "am_level" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_am_session_id" ON "public"."agent_memories" USING btree (
  "am_session_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_am_tags_gin" ON "public"."agent_memories" USING gin (
  "am_tags" "pg_catalog"."jsonb_ops"
);
CREATE INDEX "idx_am_user_id" ON "public"."agent_memories" USING btree (
  "am_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_memories
-- ----------------------------
ALTER TABLE "public"."agent_memories" ADD CONSTRAINT "agent_memories_pkey" PRIMARY KEY ("am_id");

-- ----------------------------
-- Indexes structure for table agent_tool_calls
-- ----------------------------
CREATE INDEX "idx_atc_created_at" ON "public"."agent_tool_calls" USING btree (
  "atc_created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_atc_learning_space_id" ON "public"."agent_tool_calls" USING btree (
  "atc_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_atc_message_id" ON "public"."agent_tool_calls" USING btree (
  "atc_message_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_atc_session_id" ON "public"."agent_tool_calls" USING btree (
  "atc_session_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_atc_status" ON "public"."agent_tool_calls" USING btree (
  "atc_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_atc_tool_name" ON "public"."agent_tool_calls" USING btree (
  "atc_tool_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_atc_user_id" ON "public"."agent_tool_calls" USING btree (
  "atc_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table agent_tool_calls
-- ----------------------------
ALTER TABLE "public"."agent_tool_calls" ADD CONSTRAINT "agent_tool_calls_pkey" PRIMARY KEY ("atc_id");

-- ----------------------------
-- Indexes structure for table ai_model_config
-- ----------------------------
CREATE INDEX "idx_ai_model_config_name" ON "public"."ai_model_config" USING btree (
  "amc_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ai_model_config_provider_id" ON "public"."ai_model_config" USING btree (
  "amc_provider_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ai_model_config_provider_status" ON "public"."ai_model_config" USING btree (
  "amc_provider_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "amc_status" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ai_model_config_status" ON "public"."ai_model_config" USING btree (
  "amc_status" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ai_model_config_used_for" ON "public"."ai_model_config" USING btree (
  "amc_used_for" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ai_model_config
-- ----------------------------
ALTER TABLE "public"."ai_model_config" ADD CONSTRAINT "ai_model_config_pkey" PRIMARY KEY ("amc_id");

-- ----------------------------
-- Indexes structure for table ai_provider_config
-- ----------------------------
CREATE INDEX "idx_ai_provider_config_channel" ON "public"."ai_provider_config" USING btree (
  "apc_channel" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ai_provider_config_name" ON "public"."ai_provider_config" USING btree (
  "apc_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ai_provider_config_status" ON "public"."ai_provider_config" USING btree (
  "apc_status" "pg_catalog"."int4_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ai_provider_config
-- ----------------------------
ALTER TABLE "public"."ai_provider_config" ADD CONSTRAINT "ai_provider_config_pkey" PRIMARY KEY ("apc_id");

-- ----------------------------
-- Indexes structure for table async_tasks
-- ----------------------------
CREATE INDEX "idx_async_tasks_created_at" ON "public"."async_tasks" USING btree (
  "at_created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_async_tasks_finished_at" ON "public"."async_tasks" USING btree (
  "at_finished_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_async_tasks_owner_identifier" ON "public"."async_tasks" USING btree (
  "at_owner_identifier" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_async_tasks_owner_identifier_status" ON "public"."async_tasks" USING btree (
  "at_owner_identifier" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "at_status" "pg_catalog"."int4_ops" ASC NULLS LAST
);
CREATE INDEX "idx_async_tasks_started_at" ON "public"."async_tasks" USING btree (
  "at_started_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_async_tasks_status" ON "public"."async_tasks" USING btree (
  "at_status" "pg_catalog"."int4_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table async_tasks
-- ----------------------------
ALTER TABLE "public"."async_tasks" ADD CONSTRAINT "async_tasks_pkey" PRIMARY KEY ("at_id");

-- ----------------------------
-- Indexes structure for table explanation_documents
-- ----------------------------
CREATE INDEX "idx_explanation_documents_status" ON "public"."explanation_documents" USING btree (
  "ed_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_explanation_documents_user_space" ON "public"."explanation_documents" USING btree (
  "ed_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "ed_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table explanation_documents
-- ----------------------------
ALTER TABLE "public"."explanation_documents" ADD CONSTRAINT "explanation_documents_pkey" PRIMARY KEY ("ed_id");

-- ----------------------------
-- Indexes structure for table explanation_points
-- ----------------------------
CREATE INDEX "idx_explanation_points_document" ON "public"."explanation_points" USING btree (
  "ep_explanation_document_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_explanation_points_user" ON "public"."explanation_points" USING btree (
  "ep_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table explanation_points
-- ----------------------------
ALTER TABLE "public"."explanation_points" ADD CONSTRAINT "explanation_points_pkey" PRIMARY KEY ("ep_id");

-- ----------------------------
-- Indexes structure for table explanation_relations
-- ----------------------------
CREATE INDEX "idx_explanation_relations_document" ON "public"."explanation_relations" USING btree (
  "er_explanation_document_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_explanation_relations_source" ON "public"."explanation_relations" USING btree (
  "er_source_point_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_explanation_relations_target" ON "public"."explanation_relations" USING btree (
  "er_target_point_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_explanation_relations_user" ON "public"."explanation_relations" USING btree (
  "er_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table explanation_relations
-- ----------------------------
ALTER TABLE "public"."explanation_relations" ADD CONSTRAINT "explanation_relations_pkey" PRIMARY KEY ("er_id");

-- ----------------------------
-- Indexes structure for table explanation_sections
-- ----------------------------
CREATE INDEX "idx_explanation_sections_document" ON "public"."explanation_sections" USING btree (
  "es_explanation_document_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_explanation_sections_user" ON "public"."explanation_sections" USING btree (
  "es_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table explanation_sections
-- ----------------------------
ALTER TABLE "public"."explanation_sections" ADD CONSTRAINT "explanation_sections_pkey" PRIMARY KEY ("es_id");

-- ----------------------------
-- Indexes structure for table explanation_subsections
-- ----------------------------
CREATE INDEX "idx_explanation_subsections_document" ON "public"."explanation_subsections" USING btree (
  "ess_explanation_document_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_explanation_subsections_section" ON "public"."explanation_subsections" USING btree (
  "ess_section_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_explanation_subsections_user" ON "public"."explanation_subsections" USING btree (
  "ess_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table explanation_subsections
-- ----------------------------
ALTER TABLE "public"."explanation_subsections" ADD CONSTRAINT "explanation_subsections_pkey" PRIMARY KEY ("ess_id");

-- ----------------------------
-- Indexes structure for table graph_edges
-- ----------------------------
CREATE INDEX "idx_graph_edges_graph" ON "public"."graph_edges" USING btree (
  "ge_graph_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_graph_edges_source" ON "public"."graph_edges" USING btree (
  "ge_source_virtual_node_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "ge_source_projection_node_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_graph_edges_space" ON "public"."graph_edges" USING btree (
  "ge_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_graph_edges_target" ON "public"."graph_edges" USING btree (
  "ge_target_virtual_node_id" "pg_catalog"."int8_ops" ASC NULLS LAST,
  "ge_target_projection_node_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table graph_edges
-- ----------------------------
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "graph_edges_pkey" PRIMARY KEY ("ge_id");

-- ----------------------------
-- Indexes structure for table graph_nodes
-- ----------------------------
CREATE INDEX "idx_graph_nodes_entity" ON "public"."graph_nodes" USING btree (
  "gn_entity_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_graph_nodes_graph" ON "public"."graph_nodes" USING btree (
  "gn_graph_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_graph_nodes_space" ON "public"."graph_nodes" USING btree (
  "gn_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table graph_nodes
-- ----------------------------
ALTER TABLE "public"."graph_nodes" ADD CONSTRAINT "graph_nodes_pkey" PRIMARY KEY ("gn_id");

-- ----------------------------
-- Indexes structure for table infra_file_metadata
-- ----------------------------
CREATE INDEX "idx_infra_file_metadata_access_level" ON "public"."infra_file_metadata" USING btree (
  "fm_access_level" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE INDEX "idx_infra_file_metadata_created_at" ON "public"."infra_file_metadata" USING btree (
  "fm_created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_infra_file_metadata_owner" ON "public"."infra_file_metadata" USING btree (
  "fm_owner_identifier" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_infra_file_metadata_provider" ON "public"."infra_file_metadata" USING btree (
  "fm_provider" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table infra_file_metadata
-- ----------------------------
ALTER TABLE "public"."infra_file_metadata" ADD CONSTRAINT "infra_file_metadata_pkey" PRIMARY KEY ("fm_id");

-- ----------------------------
-- Indexes structure for table knowledge_folders
-- ----------------------------
CREATE INDEX "idx_knowledge_folders_parent" ON "public"."knowledge_folders" USING btree (
  "kf_parent_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_folders_space" ON "public"."knowledge_folders" USING btree (
  "kf_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_folders_user" ON "public"."knowledge_folders" USING btree (
  "kf_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table knowledge_folders
-- ----------------------------
ALTER TABLE "public"."knowledge_folders" ADD CONSTRAINT "knowledge_folders_pkey" PRIMARY KEY ("kf_id");

-- ----------------------------
-- Indexes structure for table knowledge_graphs
-- ----------------------------
CREATE INDEX "idx_knowledge_graphs_space" ON "public"."knowledge_graphs" USING btree (
  "kg_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_graphs_user" ON "public"."knowledge_graphs" USING btree (
  "kg_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table knowledge_graphs
-- ----------------------------
ALTER TABLE "public"."knowledge_graphs" ADD CONSTRAINT "knowledge_graphs_pkey" PRIMARY KEY ("kg_id");

-- ----------------------------
-- Indexes structure for table knowledge_point_relations
-- ----------------------------
CREATE INDEX "idx_knowledge_point_relations_source" ON "public"."knowledge_point_relations" USING btree (
  "kpr_source_point_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_point_relations_space" ON "public"."knowledge_point_relations" USING btree (
  "kpr_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_point_relations_target" ON "public"."knowledge_point_relations" USING btree (
  "kpr_target_point_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_point_relations_user" ON "public"."knowledge_point_relations" USING btree (
  "kpr_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table knowledge_point_relations
-- ----------------------------
ALTER TABLE "public"."knowledge_point_relations" ADD CONSTRAINT "knowledge_point_relations_pkey" PRIMARY KEY ("kpr_id");

-- ----------------------------
-- Indexes structure for table knowledge_point_versions
-- ----------------------------
CREATE INDEX "idx_knowledge_point_versions_point" ON "public"."knowledge_point_versions" USING btree (
  "kpv_knowledge_point_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_point_versions_user" ON "public"."knowledge_point_versions" USING btree (
  "kpv_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table knowledge_point_versions
-- ----------------------------
ALTER TABLE "public"."knowledge_point_versions" ADD CONSTRAINT "knowledge_point_versions_pkey" PRIMARY KEY ("kpv_id");

-- ----------------------------
-- Indexes structure for table knowledge_points
-- ----------------------------
CREATE INDEX "idx_knowledge_points_folder" ON "public"."knowledge_points" USING btree (
  "kp_folder_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_points_space" ON "public"."knowledge_points" USING btree (
  "kp_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_knowledge_points_user" ON "public"."knowledge_points" USING btree (
  "kp_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table knowledge_points
-- ----------------------------
ALTER TABLE "public"."knowledge_points" ADD CONSTRAINT "knowledge_points_pkey" PRIMARY KEY ("kp_id");

-- ----------------------------
-- Indexes structure for table learning_spaces
-- ----------------------------
CREATE INDEX "idx_learning_spaces_description_gin" ON "public"."learning_spaces" USING gin (
  to_tsvector('simple'::regconfig, ls_description::text) "pg_catalog"."tsvector_ops"
);
CREATE INDEX "idx_learning_spaces_user_id" ON "public"."learning_spaces" USING btree (
  "ls_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table learning_spaces
-- ----------------------------
ALTER TABLE "public"."learning_spaces" ADD CONSTRAINT "learning_spaces_pkey" PRIMARY KEY ("ls_id");

-- ----------------------------
-- Indexes structure for table resource_chunks
-- ----------------------------
CREATE INDEX "idx_resource_chunks_resource_id" ON "public"."resource_chunks" USING btree (
  "rc_resource_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_resource_chunks_space_id" ON "public"."resource_chunks" USING btree (
  "rc_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_resource_chunks_user_id" ON "public"."resource_chunks" USING btree (
  "rc_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_resource_chunks_vector_id" ON "public"."resource_chunks" USING btree (
  "rc_vector_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table resource_chunks
-- ----------------------------
ALTER TABLE "public"."resource_chunks" ADD CONSTRAINT "resource_chunks_pkey" PRIMARY KEY ("rc_id");

-- ----------------------------
-- Indexes structure for table resources
-- ----------------------------
CREATE INDEX "idx_resources_space_id" ON "public"."resources" USING btree (
  "rs_learning_space_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_resources_user_id" ON "public"."resources" USING btree (
  "rs_created_by_user_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table resources
-- ----------------------------
ALTER TABLE "public"."resources" ADD CONSTRAINT "resources_pkey" PRIMARY KEY ("rs_id");

-- ----------------------------
-- Indexes structure for table user_accounts
-- ----------------------------
CREATE INDEX "idx_user_accounts_created_at" ON "public"."user_accounts" USING btree (
  "ua_created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_user_accounts_invite_code" ON "public"."user_accounts" USING btree (
  "ua_invite_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_user_accounts_inviter_id" ON "public"."user_accounts" USING btree (
  "ua_inviter_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);
CREATE INDEX "idx_user_accounts_status" ON "public"."user_accounts" USING btree (
  "ua_status" "pg_catalog"."int2_ops" ASC NULLS LAST
);
CREATE UNIQUE INDEX "uniq_email_active" ON "public"."user_accounts" USING btree (
  "ua_email" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE ua_deleted_at IS NULL AND ua_email IS NOT NULL;
CREATE UNIQUE INDEX "uniq_phone_active" ON "public"."user_accounts" USING btree (
  "ua_phone" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE ua_deleted_at IS NULL AND ua_phone IS NOT NULL;
CREATE UNIQUE INDEX "uniq_username_active" ON "public"."user_accounts" USING btree (
  "ua_username" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
) WHERE ua_deleted_at IS NULL;

-- ----------------------------
-- Triggers structure for table user_accounts
-- ----------------------------
CREATE TRIGGER "public"."update_user_accounts_updated_at" BEFORE UPDATE ON "public"."user_accounts"
FOR EACH ROW
EXECUTE PROCEDURE "public"."update_updated_at_column"();

-- ----------------------------
-- Checks structure for table user_accounts
-- ----------------------------
ALTER TABLE "public"."user_accounts" ADD CONSTRAINT "chk_email_or_phone" CHECK (ua_email IS NOT NULL OR ua_phone IS NOT NULL);

-- ----------------------------
-- Primary Key structure for table user_accounts
-- ----------------------------
ALTER TABLE "public"."user_accounts" ADD CONSTRAINT "user_accounts_pkey" PRIMARY KEY ("ua_id");

-- ----------------------------
-- Indexes structure for table vector_store_1024
-- ----------------------------
CREATE INDEX "vector_store_1024_ivfflat_index" ON "public"."vector_store_1024" (
  "embedding" "public"."vector_cosine_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table vector_store_1024
-- ----------------------------
ALTER TABLE "public"."vector_store_1024" ADD CONSTRAINT "vector_store_1024_pkey" PRIMARY KEY ("embedding_id");

-- ----------------------------
-- Indexes structure for table vector_store_1536
-- ----------------------------
CREATE INDEX "vector_store_1536_ivfflat_index" ON "public"."vector_store_1536" (
  "embedding" "public"."vector_cosine_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table vector_store_1536
-- ----------------------------
ALTER TABLE "public"."vector_store_1536" ADD CONSTRAINT "vector_store_1536_pkey" PRIMARY KEY ("embedding_id");

-- ----------------------------
-- Primary Key structure for table vector_store_2048
-- ----------------------------
ALTER TABLE "public"."vector_store_2048" ADD CONSTRAINT "vector_store_2048_pkey" PRIMARY KEY ("embedding_id");

-- ----------------------------
-- Foreign Keys structure for table agent_chat_messages
-- ----------------------------
ALTER TABLE "public"."agent_chat_messages" ADD CONSTRAINT "fk_learning_space" FOREIGN KEY ("acm_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_chat_messages" ADD CONSTRAINT "fk_sessions" FOREIGN KEY ("acm_session_id") REFERENCES "public"."agent_chat_sessions" ("acs_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_chat_messages" ADD CONSTRAINT "fk_user_accounts" FOREIGN KEY ("acm_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table agent_chat_sessions
-- ----------------------------
ALTER TABLE "public"."agent_chat_sessions" ADD CONSTRAINT "fk_acs_user" FOREIGN KEY ("acs_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_chat_sessions" ADD CONSTRAINT "fk_learning_space" FOREIGN KEY ("acs_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table agent_learning_tasks
-- ----------------------------
ALTER TABLE "public"."agent_learning_tasks" ADD CONSTRAINT "fk_alt_learning_space" FOREIGN KEY ("alt_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_learning_tasks" ADD CONSTRAINT "fk_alt_session" FOREIGN KEY ("alt_session_id") REFERENCES "public"."agent_chat_sessions" ("acs_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_learning_tasks" ADD CONSTRAINT "fk_alt_user" FOREIGN KEY ("alt_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table agent_memories
-- ----------------------------
ALTER TABLE "public"."agent_memories" ADD CONSTRAINT "fk_am_learning_space" FOREIGN KEY ("am_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_memories" ADD CONSTRAINT "fk_am_session" FOREIGN KEY ("am_session_id") REFERENCES "public"."agent_chat_sessions" ("acs_id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_memories" ADD CONSTRAINT "fk_am_user" FOREIGN KEY ("am_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table agent_tool_calls
-- ----------------------------
ALTER TABLE "public"."agent_tool_calls" ADD CONSTRAINT "fk_atc_learning_space" FOREIGN KEY ("atc_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_tool_calls" ADD CONSTRAINT "fk_atc_message" FOREIGN KEY ("atc_message_id") REFERENCES "public"."agent_chat_messages" ("acm_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_tool_calls" ADD CONSTRAINT "fk_atc_session" FOREIGN KEY ("atc_session_id") REFERENCES "public"."agent_chat_sessions" ("acs_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."agent_tool_calls" ADD CONSTRAINT "fk_atc_user" FOREIGN KEY ("atc_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table ai_model_config
-- ----------------------------
ALTER TABLE "public"."ai_model_config" ADD CONSTRAINT "fk_amc_provider_id" FOREIGN KEY ("amc_provider_id") REFERENCES "public"."ai_provider_config" ("apc_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table explanation_documents
-- ----------------------------
ALTER TABLE "public"."explanation_documents" ADD CONSTRAINT "fk_ed_learning_space" FOREIGN KEY ("ed_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."explanation_documents" ADD CONSTRAINT "fk_ed_user" FOREIGN KEY ("ed_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table explanation_points
-- ----------------------------
ALTER TABLE "public"."explanation_points" ADD CONSTRAINT "fk_ep_document" FOREIGN KEY ("ep_explanation_document_id") REFERENCES "public"."explanation_documents" ("ed_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."explanation_points" ADD CONSTRAINT "fk_ep_user" FOREIGN KEY ("ep_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table explanation_relations
-- ----------------------------
ALTER TABLE "public"."explanation_relations" ADD CONSTRAINT "fk_er_document" FOREIGN KEY ("er_explanation_document_id") REFERENCES "public"."explanation_documents" ("ed_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."explanation_relations" ADD CONSTRAINT "fk_er_source_point" FOREIGN KEY ("er_source_point_id") REFERENCES "public"."explanation_points" ("ep_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."explanation_relations" ADD CONSTRAINT "fk_er_target_point" FOREIGN KEY ("er_target_point_id") REFERENCES "public"."explanation_points" ("ep_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."explanation_relations" ADD CONSTRAINT "fk_er_user" FOREIGN KEY ("er_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table explanation_sections
-- ----------------------------
ALTER TABLE "public"."explanation_sections" ADD CONSTRAINT "fk_es_document" FOREIGN KEY ("es_explanation_document_id") REFERENCES "public"."explanation_documents" ("ed_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."explanation_sections" ADD CONSTRAINT "fk_es_user" FOREIGN KEY ("es_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table explanation_subsections
-- ----------------------------
ALTER TABLE "public"."explanation_subsections" ADD CONSTRAINT "fk_ess_document" FOREIGN KEY ("ess_explanation_document_id") REFERENCES "public"."explanation_documents" ("ed_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."explanation_subsections" ADD CONSTRAINT "fk_ess_section" FOREIGN KEY ("ess_section_id") REFERENCES "public"."explanation_sections" ("es_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."explanation_subsections" ADD CONSTRAINT "fk_ess_user" FOREIGN KEY ("ess_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table graph_edges
-- ----------------------------
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "fk_ge_graph" FOREIGN KEY ("ge_graph_id") REFERENCES "public"."knowledge_graphs" ("kg_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "fk_ge_learning_space" FOREIGN KEY ("ge_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "fk_ge_relation_entity" FOREIGN KEY ("ge_relation_entity_relation_id") REFERENCES "public"."knowledge_point_relations" ("kpr_id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "fk_ge_source_projection" FOREIGN KEY ("ge_source_projection_node_id") REFERENCES "public"."graph_nodes" ("gn_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "fk_ge_source_virtual" FOREIGN KEY ("ge_source_virtual_node_id") REFERENCES "public"."graph_nodes" ("gn_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "fk_ge_target_projection" FOREIGN KEY ("ge_target_projection_node_id") REFERENCES "public"."graph_nodes" ("gn_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "fk_ge_target_virtual" FOREIGN KEY ("ge_target_virtual_node_id") REFERENCES "public"."graph_nodes" ("gn_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_edges" ADD CONSTRAINT "fk_ge_user" FOREIGN KEY ("ge_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table graph_nodes
-- ----------------------------
ALTER TABLE "public"."graph_nodes" ADD CONSTRAINT "fk_gn_entity" FOREIGN KEY ("gn_entity_id") REFERENCES "public"."knowledge_points" ("kp_id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_nodes" ADD CONSTRAINT "fk_gn_graph" FOREIGN KEY ("gn_graph_id") REFERENCES "public"."knowledge_graphs" ("kg_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_nodes" ADD CONSTRAINT "fk_gn_learning_space" FOREIGN KEY ("gn_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."graph_nodes" ADD CONSTRAINT "fk_gn_user" FOREIGN KEY ("gn_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table knowledge_folders
-- ----------------------------
ALTER TABLE "public"."knowledge_folders" ADD CONSTRAINT "fk_kf_learning_space" FOREIGN KEY ("kf_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_folders" ADD CONSTRAINT "fk_kf_parent" FOREIGN KEY ("kf_parent_id") REFERENCES "public"."knowledge_folders" ("kf_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_folders" ADD CONSTRAINT "fk_kf_user" FOREIGN KEY ("kf_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table knowledge_graphs
-- ----------------------------
ALTER TABLE "public"."knowledge_graphs" ADD CONSTRAINT "fk_kg_learning_space" FOREIGN KEY ("kg_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_graphs" ADD CONSTRAINT "fk_kg_user" FOREIGN KEY ("kg_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table knowledge_point_relations
-- ----------------------------
ALTER TABLE "public"."knowledge_point_relations" ADD CONSTRAINT "fk_kpr_learning_space" FOREIGN KEY ("kpr_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_point_relations" ADD CONSTRAINT "fk_kpr_source_point" FOREIGN KEY ("kpr_source_point_id") REFERENCES "public"."knowledge_points" ("kp_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_point_relations" ADD CONSTRAINT "fk_kpr_target_point" FOREIGN KEY ("kpr_target_point_id") REFERENCES "public"."knowledge_points" ("kp_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_point_relations" ADD CONSTRAINT "fk_kpr_user" FOREIGN KEY ("kpr_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table knowledge_point_versions
-- ----------------------------
ALTER TABLE "public"."knowledge_point_versions" ADD CONSTRAINT "fk_kpv_knowledge_point" FOREIGN KEY ("kpv_knowledge_point_id") REFERENCES "public"."knowledge_points" ("kp_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_point_versions" ADD CONSTRAINT "fk_kpv_user" FOREIGN KEY ("kpv_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE SET NULL ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table knowledge_points
-- ----------------------------
ALTER TABLE "public"."knowledge_points" ADD CONSTRAINT "fk_kp_current_version" FOREIGN KEY ("kp_current_version_id") REFERENCES "public"."knowledge_point_versions" ("kpv_id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_points" ADD CONSTRAINT "fk_kp_folder" FOREIGN KEY ("kp_folder_id") REFERENCES "public"."knowledge_folders" ("kf_id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_points" ADD CONSTRAINT "fk_kp_user" FOREIGN KEY ("kp_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."knowledge_points" ADD CONSTRAINT "fk_learning_space" FOREIGN KEY ("kp_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table learning_spaces
-- ----------------------------
ALTER TABLE "public"."learning_spaces" ADD CONSTRAINT "fk_user_accounts" FOREIGN KEY ("ls_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table resource_chunks
-- ----------------------------
ALTER TABLE "public"."resource_chunks" ADD CONSTRAINT "fk_rc_learning_space" FOREIGN KEY ("rc_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."resource_chunks" ADD CONSTRAINT "fk_rc_user" FOREIGN KEY ("rc_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."resource_chunks" ADD CONSTRAINT "fk_resource_chunks_resource_id" FOREIGN KEY ("rc_resource_id") REFERENCES "public"."resources" ("rs_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table resources
-- ----------------------------
ALTER TABLE "public"."resources" ADD CONSTRAINT "fk_rs_learning_space" FOREIGN KEY ("rs_learning_space_id") REFERENCES "public"."learning_spaces" ("ls_id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."resources" ADD CONSTRAINT "fk_rs_user" FOREIGN KEY ("rs_created_by_user_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE CASCADE ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table user_accounts
-- ----------------------------
ALTER TABLE "public"."user_accounts" ADD CONSTRAINT "fk_inviter" FOREIGN KEY ("ua_inviter_id") REFERENCES "public"."user_accounts" ("ua_id") ON DELETE SET NULL ON UPDATE NO ACTION;
